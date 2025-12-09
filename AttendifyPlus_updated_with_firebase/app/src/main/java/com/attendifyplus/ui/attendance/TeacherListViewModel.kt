package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TeacherListViewModel(
    private val repository: TeacherRepository,
    private val studentRepository: StudentRepository? = null // Optional for backward compatibility or Koin injection flexibility
) : ViewModel() {

    private val _teachers = MutableStateFlow<List<TeacherEntity>>(emptyList())
    val teachers: StateFlow<List<TeacherEntity>> = _teachers.asStateFlow()

    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students.asStateFlow()

    init {
        loadTeachers()
        loadStudents()
    }

    private fun loadTeachers() {
        viewModelScope.launch {
            repository.getAllFlow().collect { list ->
                _teachers.value = list
            }
        }
    }

    private fun loadStudents() {
        if (studentRepository != null) {
            viewModelScope.launch {
                studentRepository.getAll().collect { list ->
                    _students.value = list
                }
            }
        }
    }

    // Updated to accept a full Entity object
    fun addTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
             repository.insert(teacher)
        }
    }
    
    fun updateTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
             repository.insert(teacher) // Insert acts as upsert if using OnConflictStrategy.REPLACE
        }
    }

    fun deleteTeacher(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun resetCredentials(teacherId: String, username: String, password: String) {
        viewModelScope.launch {
            repository.updateCredentials(teacherId, username, password)
        }
    }

    fun resetStudentCredentials(studentId: String, username: String, password: String) {
        if (studentRepository != null) {
            viewModelScope.launch {
                studentRepository.updateCredentials(studentId, username, password)
            }
        }
    }
}
