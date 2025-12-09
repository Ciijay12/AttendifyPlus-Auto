package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.data.repositories.SchoolCalendarConfigRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AcademicPeriodsViewModel(
    private val schoolPeriodRepo: SchoolPeriodRepository,
    private val calendarConfigRepo: SchoolCalendarConfigRepository
) : ViewModel() {

    private val _schoolPeriod = MutableStateFlow<SchoolPeriodEntity?>(null)
    val schoolPeriod: StateFlow<SchoolPeriodEntity?> = _schoolPeriod.asStateFlow()

    private val _initialSchoolPeriod = MutableStateFlow<SchoolPeriodEntity?>(null)

    val isModified: StateFlow<Boolean> = combine(_schoolPeriod, _initialSchoolPeriod) { current, initial ->
        current != initial
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val calendarConfig: StateFlow<SchoolCalendarConfigEntity?> = calendarConfigRepo.get()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            // Collect once to initialize, then stop listening to avoid overwriting local changes
            val period = schoolPeriodRepo.periodFlow.firstOrNull()
            if (period != null) {
                _schoolPeriod.value = period
                _initialSchoolPeriod.value = period
            }
        }
    }

    fun onDateChanged(periodKey: String, date: Long) {
        val current = _schoolPeriod.value ?: SchoolPeriodEntity(
            schoolYear = calendarConfig.value?.schoolYear ?: "",
            q1Start = 0L, q1End = 0L, q2Start = 0L, q2End = 0L, q3Start = 0L, q3End = 0L, q4Start = 0L, q4End = 0L
        )
        _schoolPeriod.value = when (periodKey) {
            "jhsQ1Start" -> current.copy(q1Start = date)
            "jhsQ1End" -> current.copy(q1End = date)
            "jhsQ2Start" -> current.copy(q2Start = date)
            "jhsQ2End" -> current.copy(q2End = date)
            "jhsQ3Start" -> current.copy(q3Start = date)
            "jhsQ3End" -> current.copy(q3End = date)
            "jhsQ4Start" -> current.copy(q4Start = date)
            "jhsQ4End" -> current.copy(q4End = date)
            "shsQ1Start" -> current.copy(shsQ1Start = date)
            "shsQ1End" -> current.copy(shsQ1End = date)
            "shsQ2Start" -> current.copy(shsQ2Start = date)
            "shsQ2End" -> current.copy(shsQ2End = date)
            "shsQ3Start" -> current.copy(shsQ3Start = date)
            "shsQ3End" -> current.copy(shsQ3End = date)
            "shsQ4Start" -> current.copy(shsQ4Start = date)
            "shsQ4End" -> current.copy(shsQ4End = date)
            else -> current
        }
    }
    
    fun applyJhsToShs() {
        val current = _schoolPeriod.value ?: return
        _schoolPeriod.value = current.copy(
            shsQ1Start = current.q1Start, shsQ1End = current.q1End,
            shsQ2Start = current.q2Start, shsQ2End = current.q2End,
            shsQ3Start = current.q3Start, shsQ3End = current.q3End,
            shsQ4Start = current.q4Start, shsQ4End = current.q4End
        )
    }

    fun savePeriods() {
        viewModelScope.launch {
            _schoolPeriod.value?.let {
                val periodToSave = it.copy(schoolYear = calendarConfig.value?.schoolYear ?: it.schoolYear)
                schoolPeriodRepo.insert(periodToSave)
                _initialSchoolPeriod.value = periodToSave // Update the initial state to reflect the saved state
            }
        }
    }
}
