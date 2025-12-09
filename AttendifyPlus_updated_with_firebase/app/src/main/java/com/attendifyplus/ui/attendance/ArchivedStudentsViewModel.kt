package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchivedStudentsViewModel(private val studentRepo: StudentRepository) : ViewModel() {

    private val _archivedStudents = MutableStateFlow<List<StudentEntity>>(emptyList())
    val archivedStudents: StateFlow<List<StudentEntity>> = _archivedStudents.asStateFlow()

    init {
        loadArchivedStudents()
    }

    private fun loadArchivedStudents() {
        viewModelScope.launch {
            studentRepo.getArchivedStudents().collect {
                _archivedStudents.value = it
            }
        }
    }

    fun restoreStudent(studentId: String) {
        viewModelScope.launch {
            studentRepo.restore(studentId)
        }
    }
}