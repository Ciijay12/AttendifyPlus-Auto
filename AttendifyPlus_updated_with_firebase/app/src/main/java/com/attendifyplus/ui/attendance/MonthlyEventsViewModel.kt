package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.data.repositories.SchoolEventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MonthlyEventsViewModel(
    private val eventRepo: SchoolEventRepository,
    private val year: Int,
    private val month: Int
) : ViewModel() {

    val events: StateFlow<List<SchoolEventEntity>> = eventRepo.getEventsForMonth(year, month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEvent(title: String, description: String, date: Date) {
        viewModelScope.launch {
            val newEvent = SchoolEventEntity(
                date = date.time,
                title = title,
                description = description,
                type = "activity",
                isNoClass = false,
                synced = false
            )
            eventRepo.addEvent(newEvent)
        }
    }

    fun deleteEvent(event: SchoolEventEntity) {
        viewModelScope.launch {
            eventRepo.deleteEvent(event.id)
        }
    }
}
