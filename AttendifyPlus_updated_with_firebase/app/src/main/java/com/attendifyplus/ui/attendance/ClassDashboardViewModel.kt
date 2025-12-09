package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.sync.SyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

data class StudentWithStatus(
    val student: StudentEntity,
    val status: String // "present", "late", "absent", "none"
)

class ClassDashboardViewModel(
    private val attendanceRepo: AttendanceRepository,
    private val studentRepo: StudentRepository
) : ViewModel() {

    private val _presentCount = MutableStateFlow(0)
    val presentCount: StateFlow<Int> = _presentCount

    private val _lateCount = MutableStateFlow(0)
    val lateCount: StateFlow<Int> = _lateCount

    private val _absentCount = MutableStateFlow(0)
    val absentCount: StateFlow<Int> = _absentCount

    private val _history = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val history: StateFlow<List<AttendanceEntity>> = _history.asStateFlow()

    private val _students = MutableStateFlow<List<StudentWithStatus>>(emptyList())
    val students: StateFlow<List<StudentWithStatus>> = _students.asStateFlow()
    
    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    // Sorting
    private var currentSort = SortOrder.NAME_ASC
    private var currentStudentsList = listOf<StudentWithStatus>()

    enum class SortOrder { NAME_ASC, NAME_DESC }

    fun loadStatsForSubject(subjectName: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayMidnight = calendar.timeInMillis

        viewModelScope.launch {
            attendanceRepo.getPresentCountForSubject(todayMidnight, subjectName).collectLatest {
                _presentCount.value = it
            }
        }
        viewModelScope.launch {
            attendanceRepo.getLateCountForSubject(todayMidnight, subjectName).collectLatest {
                _lateCount.value = it
            }
        }
        viewModelScope.launch {
            attendanceRepo.getAbsentCountForSubject(todayMidnight, subjectName).collectLatest {
                _absentCount.value = it
            }
        }
        
        viewModelScope.launch {
            attendanceRepo.getClassHistory(subjectName).collectLatest {
                _history.value = it
            }
        }
    }

    fun loadStudents(grade: String, section: String, subjectName: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayMidnight = calendar.timeInMillis

        viewModelScope.launch {
            combine(
                studentRepo.getByClass(grade, section),
                attendanceRepo.getClassHistory(subjectName)
            ) { enrolled, historyList ->
                val todayRecords = historyList.filter { it.timestamp >= todayMidnight }
                
                enrolled.map { student ->
                    val record = todayRecords.find { it.studentId == student.id }
                    StudentWithStatus(student, record?.status ?: "none")
                }
            }.collectLatest { mappedList ->
                currentStudentsList = mappedList
                sortStudents(currentSort)
            }
        }
    }

    fun toggleSort() {
        currentSort = if (currentSort == SortOrder.NAME_ASC) SortOrder.NAME_DESC else SortOrder.NAME_ASC
        sortStudents(currentSort)
    }

    private fun sortStudents(order: SortOrder) {
        val sorted = when (order) {
            SortOrder.NAME_ASC -> currentStudentsList.sortedBy { "${it.student.lastName} ${it.student.firstName}" }
            SortOrder.NAME_DESC -> currentStudentsList.sortedByDescending { "${it.student.lastName} ${it.student.firstName}" }
        }
        _students.value = sorted
    }

    fun deleteAttendance(id: Long) {
        viewModelScope.launch {
            attendanceRepo.deleteById(id)
        }
    }
    
    // Feature 2: Adviser Reset Capability
    fun resetStudentCredentials(studentId: String) {
        viewModelScope.launch {
            val student = studentRepo.getById(studentId) ?: return@launch
            val updatedStudent = student.copy(
                username = null, // Reset to default
                password = null, // Reset to default
                hasChangedCredentials = false
            )
            studentRepo.insert(updatedStudent)
        }
    }
    
    // New Feature: Add Student directly from Class Dashboard
    fun addStudent(id: String, first: String, last: String, grade: String, section: String) {
        viewModelScope.launch {
            // Ensure uniqueness (basic check, Room will handle conflict but best to check)
            if (studentRepo.getById(id) == null) {
                studentRepo.insert(
                    StudentEntity(
                        id = id, 
                        firstName = first, 
                        lastName = last, 
                        grade = grade, 
                        section = section,
                        username = null, // Default
                        password = null, // Default
                        hasChangedCredentials = false,
                        isArchived = false // Default
                    )
                )
            } else {
                val existing = studentRepo.getById(id)
                if (existing != null && existing.isArchived) {
                    studentRepo.restore(id)
                }
            }
        }
    }
    
    fun fetchStudents(context: Context) {
        _importStatus.value = "Starting sync..."
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        val workManager = WorkManager.getInstance(context)
        
        workManager.enqueue(workRequest)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var finished = false
                while (!finished) {
                    val info = workManager.getWorkInfoById(workRequest.id).get()
                    if (info != null) {
                        when (info.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                _importStatus.value = "Sync Successful!"
                                finished = true
                            }
                            WorkInfo.State.FAILED -> {
                                _importStatus.value = "Sync Failed."
                                finished = true
                            }
                            WorkInfo.State.CANCELLED -> {
                                _importStatus.value = "Sync Cancelled."
                                finished = true
                            }
                            WorkInfo.State.RUNNING -> {
                                // Only update if not already set to avoid spamming
                                if (_importStatus.value != "Syncing...") {
                                     _importStatus.value = "Syncing..."
                                }
                            }
                            else -> {
                                // ENQUEUED, BLOCKED
                            }
                        }
                    }
                    if (!finished) {
                        kotlinx.coroutines.delay(500)
                    }
                }
            } catch (e: Exception) {
                 _importStatus.value = "Sync initiated in background."
            }
        }
    }
    
    fun clearImportStatus() {
        _importStatus.value = null
    }

    // Update Student Name
    fun updateStudentName(studentId: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            val existing = studentRepo.getById(studentId)
            if (existing != null) {
                studentRepo.insert(existing.copy(firstName = firstName, lastName = lastName))
            }
        }
    }

    // Update Attendance Status
    fun updateAttendanceStatus(studentId: String, status: String, subjectName: String) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayMidnight = calendar.timeInMillis
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            val historyList = _history.value
            val todayRecord = historyList.find { it.studentId == studentId && it.timestamp >= todayMidnight }

            if (status == "none") {
                if (todayRecord != null) {
                    attendanceRepo.deleteById(todayRecord.id)
                }
            } else {
                if (todayRecord != null) {
                    // Update existing
                    attendanceRepo.record(todayRecord.copy(status = status, synced = false))
                } else {
                    // Insert new
                    attendanceRepo.record(
                        AttendanceEntity(
                            studentId = studentId,
                            timestamp = now,
                            status = status,
                            type = "subject",
                            subject = subjectName
                        )
                    )
                }
            }
        }
    }
    
    // Renamed from deleteStudent to archiveStudent
    fun archiveStudent(studentId: String) {
        viewModelScope.launch {
            // Archive student (set isArchived = 1)
            studentRepo.archive(studentId)
        }
    }

    fun restoreStudent(studentId: String) {
        viewModelScope.launch {
            studentRepo.restore(studentId)
        }
    }
    
    fun generateId(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) % 100
        val random = (1000..9999).random()
        return "I-$year-$random"
    }
}
