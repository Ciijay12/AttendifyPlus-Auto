package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StudentHistoryViewModel(
    repository: AttendanceRepository,
    studentId: String = SessionManager.currentStudentId 
) : ViewModel() {

    val history: StateFlow<List<AttendanceEntity>> = repository.getStudentHistory(studentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
