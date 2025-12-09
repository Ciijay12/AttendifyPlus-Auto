package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class SubjectWithStatus(
    val subject: SubjectClassEntity,
    val status: String // present, late, absent, or unmarked
)

class StudentDashboardViewModel(
    private val studentRepo: StudentRepository,
    private val attendanceRepo: AttendanceRepository,
    private val subjectClassRepo: SubjectClassRepository,
    private val schoolEventRepo: SchoolEventRepository,
    private val schoolPeriodRepo: SchoolPeriodRepository
) : ViewModel() {

    private val _subjectClassesWithStatus = MutableStateFlow<List<SubjectWithStatus>>(emptyList())
    val subjectClassesWithStatus: StateFlow<List<SubjectWithStatus>> = _subjectClassesWithStatus.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    private val _studentGradeSection = MutableStateFlow("")
    val studentGradeSection: StateFlow<String> = _studentGradeSection.asStateFlow()
    
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
                .take(2) // Limit to 2 events
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadStudentDetails(studentId: String) {
        currentStudentId = studentId
        viewModelScope.launch {
            val student = studentRepo.getById(studentId)
            _userName.value = student?.firstName ?: "Student"
            _studentGradeSection.value = if (student != null) "Grade ${student.grade} - ${student.section}" else ""
            _hasChangedCredentials.value = student?.hasChangedCredentials ?: false

            if (student != null) {
                // Combine subject classes with their attendance status for today
                combine(
                    subjectClassRepo.getClassesByGradeAndSection(student.grade, student.section),
                    attendanceRepo.getStudentHistory(student.id)
                ) { subjects, history ->
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
                    
                    subjects.map { subject ->
                        val record = todaysRecords.find { it.subject == subject.subjectName }
                        SubjectWithStatus(subject, record?.status ?: "unmarked")
                    }
                }.collect { classesWithStatus ->
                    _subjectClassesWithStatus.value = classesWithStatus
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

    fun refresh() {
        val id = currentStudentId ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadStudentDetails(id)
            delay(1500) 
            _isRefreshing.value = false
        }
    }
}
