package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.SchoolEventRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.SubjectClassRepository
import com.attendifyplus.data.repositories.TeacherRepository
import com.attendifyplus.sync.SyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class SubjectWithStatus(
    val subject: SubjectClassEntity,
    val status: String, // present, late, absent, or unmarked
    val teacherName: String // Added teacher name
)

data class StudentAdvisoryInfo(
    val grade: String = "",
    val section: String = "",
    val adviserName: String = "Unassigned",
    val track: String? = null // For SHS
)

class StudentDashboardViewModel(
    private val studentRepo: StudentRepository,
    private val attendanceRepo: AttendanceRepository,
    private val subjectClassRepo: SubjectClassRepository,
    private val schoolEventRepo: SchoolEventRepository,
    private val schoolPeriodRepo: SchoolPeriodRepository,
    private val teacherRepo: TeacherRepository, // Added TeacherRepository
    private val context: Context
) : ViewModel() {

    private val _subjectClassesWithStatus = MutableStateFlow<List<SubjectWithStatus>>(emptyList())
    val subjectClassesWithStatus: StateFlow<List<SubjectWithStatus>> = _subjectClassesWithStatus.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    // New State for Advisory Card
    private val _advisoryInfo = MutableStateFlow(StudentAdvisoryInfo())
    val advisoryInfo: StateFlow<StudentAdvisoryInfo> = _advisoryInfo.asStateFlow()
    
    private val _hasChangedCredentials = MutableStateFlow(false)
    val hasChangedCredentials: StateFlow<Boolean> = _hasChangedCredentials.asStateFlow()
    
    private var currentStudentId: String? = null

    val schoolPeriod: StateFlow<SchoolPeriodEntity?> = schoolPeriodRepo.periodFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyStatus: StateFlow<String> = schoolEventRepo.getAllEvents()
        .map { events ->
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val noClassEvent = events.find { event ->
                val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
                val sameDay = eventCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                              eventCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                
                sameDay && event.isNoClass
            }
            
            if (noClassEvent != null) noClassEvent.title else "Class Day"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Class Day")

    val upcomingEvents: StateFlow<List<SchoolEventEntity>> = schoolEventRepo.getAllEvents()
        .map { events ->
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            events
                .filter { it.date >= today && !it.isNoClass } 
                .sortedBy { it.date }
                .take(2) 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun loadStudentDetails(studentId: String) {
        currentStudentId = studentId
        
        // Use Flow collection instead of one-shot fetch for responsiveness to sync
        viewModelScope.launch {
            studentRepo.getByIdFlow(studentId).collect { student ->
                _userName.value = student?.firstName ?: "Student"
                _hasChangedCredentials.value = student?.hasChangedCredentials ?: false

                if (student != null) {
                    // Fetch Advisory Info (Adviser)
                    val teachers = teacherRepo.getAll() 
                    val adviser = teachers.find { 
                        it.advisoryGrade == student.grade && it.advisorySection == student.section 
                    }
                    
                    _advisoryInfo.value = StudentAdvisoryInfo(
                        grade = student.grade,
                        section = student.section,
                        adviserName = if (adviser != null) "${adviser.firstName} ${adviser.lastName}" else "Unassigned",
                        track = adviser?.advisoryTrack // Get track from adviser
                    )

                    // Fetch ALL classes and filter manually to be robust against format mismatches
                    // e.g. "11" vs "Grade 11", "Section A" vs "A"
                    
                    combine(
                        subjectClassRepo.getAllClassesFlow(), // Use the new flow for all classes
                        attendanceRepo.getStudentHistory(student.id)
                    ) { allClasses, history ->
                        // 1. Normalize Student Info
                        val sGrade = student.grade.filter { it.isDigit() } // "Grade 11" -> "11"
                        val sSectionRaw = student.section.trim().lowercase() // " Section A " -> "section a"
                        // Strip "section" word if present for cleaner matching
                        val sSection = sSectionRaw.replace("section", "").trim()

                        // 2. Filter Classes
                        val enrolledClasses = allClasses.filter { cls ->
                            val cGrade = cls.gradeLevel.filter { it.isDigit() }
                            val cSectionRaw = cls.section.trim().lowercase()
                            val cSection = cSectionRaw.replace("section", "").trim()
                            
                            // Check match (Allow fuzzy section match if needed, but exact trim/lower is usually safe)
                            // If grade is missing digits (e.g. "Kinder"), fallback to full string match
                            val gradeMatch = if (sGrade.isNotEmpty() && cGrade.isNotEmpty()) sGrade == cGrade else student.grade == cls.gradeLevel
                            
                            // Section match: Strict equality after cleaning OR containment if one is just "A" and other is "St. Augustine - A"
                            val sectionMatch = sSection == cSection || 
                                               (sSection.isNotEmpty() && cSection.contains(sSection)) || 
                                               (cSection.isNotEmpty() && sSection.contains(cSection))
                            
                            gradeMatch && sectionMatch
                        }

                        val todayStart = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val todayEnd = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }.timeInMillis

                        val todaysRecords = history.filter { it.timestamp in todayStart..todayEnd }
                        
                        val resultList = mutableListOf<SubjectWithStatus>()
                        
                        for (subject in enrolledClasses) {
                            val record = todaysRecords.find { it.subject == subject.subjectName }
                            val subjTeacher = teacherRepo.getById(subject.teacherId)
                            val teacherName = if (subjTeacher != null) "${subjTeacher.firstName} ${subjTeacher.lastName}" else "Unknown Teacher"
                            
                            resultList.add(SubjectWithStatus(subject, record?.status ?: "unmarked", teacherName))
                        }
                        resultList
                    }.collect { classesWithStatus ->
                        _subjectClassesWithStatus.value = classesWithStatus
                    }
                }
            }
        }
    }
    
    fun updateCredentials(username: String, password: String) {
        val id = currentStudentId ?: return
        viewModelScope.launch {
            val student = studentRepo.getById(id) ?: return@launch
            val updatedStudent = student.copy(
                username = username,
                password = password,
                hasChangedCredentials = true
            )
            studentRepo.insert(updatedStudent)
            _hasChangedCredentials.value = true
        }
    }

    fun refresh(force: Boolean = false) {
        val id = currentStudentId ?: return
         if (force) {
            triggerSync()
        } else {
            viewModelScope.launch {
                _syncState.value = SyncState.Loading
                
                // Explicitly sync Period to fix potential missing data
                try {
                    schoolPeriodRepo.syncPeriod()
                } catch (e: Exception) {
                    // Ignore, sync might fail if offline but data should be in DB if ever synced
                }
                
                // Forcing reload isn't strictly necessary with Flow, but good for UX feedback
                // and to trigger side-effects if needed.
                // Since loadStudentDetails sets up a permanent collection, re-calling it is risky (might dup collectors).
                // Better to just delay or let Flow handle updates.
                // But user might want to re-fetch "teachers" which are not flowed in this VM.
                // So re-calling is okay if we handle job cancellation or if it's cheap.
                // Here, loadStudentDetails launches a new coroutine.
                // To be safe, let's just let the Flows update naturally and delay for visual effect.
                
                delay(1000)
                _syncState.value = SyncState.Success
                delay(2000)
                _syncState.value = SyncState.Idle
            }
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            val workManager = WorkManager.getInstance(context)
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setId(UUID.randomUUID())
                .addTag("manual_sync")
                .build()

            workManager.enqueue(syncRequest)

            delay(3000)
            _syncState.value = SyncState.Success
            delay(2000)
            _syncState.value = SyncState.Idle
        }
    }
}
