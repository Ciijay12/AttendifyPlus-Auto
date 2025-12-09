package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.data.repositories.SchoolEventRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import com.attendifyplus.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class SchoolEventsViewModel(
    private val repository: SchoolEventRepository,
    private val periodRepository: SchoolPeriodRepository,
    private val notificationHelper: NotificationHelper // Injected
) : ViewModel() {

    private val _events = MutableStateFlow<List<SchoolEventEntity>>(emptyList())
    val events: StateFlow<List<SchoolEventEntity>> = _events.asStateFlow()

    val todayEvent = _events.map { list ->
        val today = normalizeDate(System.currentTimeMillis())
        list.find { it.date == today }
    }
    
    private val _schoolPeriod = MutableStateFlow<SchoolPeriodEntity?>(null)
    val schoolPeriod: StateFlow<SchoolPeriodEntity?> = _schoolPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllEvents().collect {
                _events.value = it
            }
        }
        viewModelScope.launch {
            periodRepository.periodFlow.collect {
                _schoolPeriod.value = it
            }
        }
    }

    fun updateSchoolPeriods(
        q1Start: Long, q1End: Long,
        q2Start: Long, q2End: Long,
        q3Start: Long, q3End: Long,
        q4Start: Long, q4End: Long,
        shsQ1Start: Long, shsQ1End: Long,
        shsQ2Start: Long, shsQ2End: Long,
        shsQ3Start: Long, shsQ3End: Long,
        shsQ4Start: Long, shsQ4End: Long
    ) {
        viewModelScope.launch {
            val period = SchoolPeriodEntity(
                id = 1, // Singleton
                schoolYear = "${Calendar.getInstance().get(Calendar.YEAR)}-${Calendar.getInstance().get(Calendar.YEAR)+1}",
                q1Start = q1Start, q1End = q1End,
                q2Start = q2Start, q2End = q2End,
                q3Start = q3Start, q3End = q3End,
                q4Start = q4Start, q4End = q4End,
                shsQ1Start = shsQ1Start, shsQ1End = shsQ1End,
                shsQ2Start = shsQ2Start, shsQ2End = shsQ2End,
                shsQ3Start = shsQ3Start, shsQ3End = shsQ3End,
                shsQ4Start = shsQ4Start, shsQ4End = shsQ4End
            )
            periodRepository.insert(period)
            notificationHelper.showSyncNotification("Configuration Updated", "School periods updated")
        }
    }

    fun addHoliday(timestamp: Long, title: String) {
        viewModelScope.launch {
            repository.addEvent(
                SchoolEventEntity(
                    date = normalizeDate(timestamp),
                    title = title,
                    type = "holiday",
                    isNoClass = true
                )
            )
            notificationHelper.showEventNotification("Holiday Added", "New holiday: $title")
        }
    }

    fun addSchoolActivity(title: String) {
        viewModelScope.launch {
            val today = normalizeDate(System.currentTimeMillis())
            repository.addEvent(
                SchoolEventEntity(
                    date = today,
                    title = title,
                    type = "activity",
                    isNoClass = true
                )
            )
            notificationHelper.showEventNotification("Activity Added", "Today's activity: $title")
        }
    }

    fun suspendClassesToday(reason: String) {
        viewModelScope.launch {
            val today = normalizeDate(System.currentTimeMillis())
            repository.addEvent(
                SchoolEventEntity(
                    date = today,
                    title = "Suspended",
                    description = reason,
                    type = "suspension",
                    isNoClass = true
                )
            )
            notificationHelper.showEventNotification("Classes Suspended", "Classes suspended today: $reason")
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }

    private fun normalizeDate(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
