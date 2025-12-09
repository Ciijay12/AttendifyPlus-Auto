package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdvisoryDetailsViewModel(
    private val repository: TeacherRepository,
    private val teacherId: String = "T001" // Default to Demo Teacher
) : ViewModel() {

    private val _teacher = MutableStateFlow<TeacherEntity?>(null)
    val teacher: StateFlow<TeacherEntity?> = _teacher

    init {
        loadTeacher()
    }

    private fun loadTeacher() {
        viewModelScope.launch {
            _teacher.value = repository.getById(teacherId)
        }
    }

    fun saveDetails(grade: String, section: String, startTime: String?) {
        viewModelScope.launch {
            val current = repository.getById(teacherId)
            if (current == null) {
                // Insert default teacher if missing (Demo mode fix)
                val newTeacher = TeacherEntity(
                    id = teacherId,
                    username = "teacher",
                    firstName = "John",
                    lastName = "Doe",
                    email = "teacher@demo.com",
                    role = "adviser",
                    advisoryGrade = grade,
                    advisorySection = section,
                    advisoryStartTime = startTime
                )
                repository.insert(newTeacher)
            } else {
                repository.updateAdvisoryDetails(teacherId, grade, section, startTime)
            }
            loadTeacher() 
        }
    }

    fun deleteClass() {
        viewModelScope.launch {
            repository.updateAdvisoryDetails(teacherId, null, null, null)
            loadTeacher()
        }
    }
}
