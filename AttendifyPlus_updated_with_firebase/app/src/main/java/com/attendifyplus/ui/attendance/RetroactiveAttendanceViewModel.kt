package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class RetroactiveAttendanceViewModel(
    private val studentRepo: StudentRepository,
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students.asStateFlow()

    private val _saveState = MutableStateFlow<String?>(null)
    val saveState: StateFlow<String?> = _saveState

    init {
        loadAllStudents()
    }

    private fun loadAllStudents() {
        viewModelScope.launch {
            studentRepo.getAll().collect { list ->
                // Sort by name for easier finding
                _students.value = list.sortedBy { it.lastName }
            }
        }
    }

    fun saveAttendance(
        dateTimestamp: Long,
        selectedStudentIds: List<String>,
        status: String = "present",
        type: String = "homeroom",
        subjectName: String? = null
    ) {
        viewModelScope.launch {
            try {
                val records = selectedStudentIds.map { studentId ->
                    AttendanceEntity(
                        studentId = studentId,
                        timestamp = dateTimestamp,
                        status = status,
                        type = type,
                        subject = subjectName,
                        synced = false
                    )
                }
                
                // We iterate and record. 
                // Ideally, repo should support insertAll for attendance, but record() is safe.
                records.forEach { attendanceRepo.record(it) }
                
                _saveState.value = "Success: Saved ${records.size} records."
            } catch (e: Exception) {
                _saveState.value = "Error: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _saveState.value = null
    }
}
