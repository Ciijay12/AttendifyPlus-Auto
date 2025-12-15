package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.attendifyplus.data.local.entities.*
import com.attendifyplus.data.repositories.*
import com.attendifyplus.sync.SyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar
import java.util.UUID

class TeacherDashboardViewModel(
    private val teacherRepo: TeacherRepository,
    private val studentRepo: StudentRepository,
    private val subjectClassRepo: SubjectClassRepository,
    private val schoolEventRepo: SchoolEventRepository,
    private val attendanceRepo: AttendanceRepository,
    private val schoolPeriodRepo: SchoolPeriodRepository,
    private val context: Context // Injected context for WorkManager
) : ViewModel() {

    private val prefs = context.getSharedPreferences("attendify_session", Context.MODE_PRIVATE)
    private val teacherId = prefs.getString("user_id", null) ?: "T001"

    val allStudents: StateFlow<List<StudentEntity>> = studentRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedCount: StateFlow<Int> = attendanceRepo.unsyncedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _adviserDetails = MutableStateFlow<TeacherEntity?>(null)
    val adviserDetails: StateFlow<TeacherEntity?> = _adviserDetails.asStateFlow()

    private val _studentCount = MutableStateFlow(0)
    val studentCount: StateFlow<Int> = _studentCount.asStateFlow()

    private val _currentTeacherId = MutableStateFlow(teacherId)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val subjectClasses: StateFlow<List<SubjectClassEntity>> = _currentTeacherId
        .flatMapLatest { id -> subjectClassRepo.getClassesForTeacher(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    val dailyStatus: StateFlow<String> = schoolEventRepo.getAllEvents()
        .map { events ->
            val noClassEvent = events.find { event ->
                val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
                val nowCal = Calendar.getInstance()
                val sameDay = eventCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                              eventCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
                
                sameDay && event.isNoClass
            }
            
            noClassEvent?.title ?: "Class Day"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Class Day")
    
    val schoolPeriod: StateFlow<SchoolPeriodEntity?> = schoolPeriodRepo.periodFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isCalendarConfigured: StateFlow<Boolean> = schoolPeriod.map { it != null && it.schoolYear.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val upcomingEvents: StateFlow<List<SchoolEventEntity>> = schoolEventRepo.getAllEvents()
        .map { events ->
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            events
                .filter { it.date >= today && it.type != "status" } // Filter out status events
                .sortedBy { it.date }
                .take(2) 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadAdviserDetails(teacherId)
    }

    fun loadAdviserDetails(teacherId: String) {
        _currentTeacherId.value = teacherId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            teacherRepo.getByIdFlow(teacherId).collect { teacher ->
                _adviserDetails.value = teacher
                _userName.value = teacher?.firstName ?: "Teacher"
                val grade = teacher?.advisoryGrade
                val section = teacher?.advisorySection
                if (grade != null && section != null) {
                    _studentCount.value =
                        studentRepo.countByClass(grade, section)
                } else {
                    _studentCount.value = 0
                }
            }
        }
    }

    fun updateDailyStatus(isNoClass: Boolean, reason: String) {
        viewModelScope.launch {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val allEvents = schoolEventRepo.getAllEvents().first()
            
            val todayNoClassEvents = allEvents.filter { event ->
                val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
                val sameDay = eventCal.get(Calendar.YEAR) == todayStart.get(Calendar.YEAR) &&
                              eventCal.get(Calendar.DAY_OF_YEAR) == todayStart.get(Calendar.DAY_OF_YEAR)
                sameDay && event.isNoClass
            }

            if (isNoClass) {
                todayNoClassEvents.forEach { schoolEventRepo.deleteEvent(it.id) }

                val newEvent = SchoolEventEntity(
                    date = todayStart.timeInMillis,
                    title = reason,
                    description = "Manual Status Update",
                    type = "status", // Changed from "holiday" to "status"
                    isNoClass = true,
                    synced = false
                )
                schoolEventRepo.addEvent(newEvent)
            } else {
                todayNoClassEvents.forEach { schoolEventRepo.deleteEvent(it.id) }
            }
        }
    }

    fun refresh(force: Boolean = false) {
        if (force) {
            triggerSync()
        } else {
            viewModelScope.launch {
                 _syncState.value = SyncState.Loading
                 loadAdviserDetails(_currentTeacherId.value)
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
            
            // For student/teacher, we just show loading and assume it works.
            // The UI will update reactively as data flows in.
            delay(3000) // Show loading for a few seconds for feedback
            _syncState.value = SyncState.Success
            delay(2000)
            _syncState.value = SyncState.Idle
        }
    }
}
