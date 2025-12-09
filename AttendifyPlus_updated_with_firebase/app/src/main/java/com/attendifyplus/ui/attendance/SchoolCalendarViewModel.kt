package com.attendifyplus.ui.attendance

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.data.repositories.SchoolCalendarConfigRepository
import com.attendifyplus.data.repositories.SchoolEventRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SchoolCalendarViewModel(
    private val configRepo: SchoolCalendarConfigRepository,
    private val eventRepo: SchoolEventRepository,
    private val periodRepo: SchoolPeriodRepository // Injected
) : ViewModel() {

    val config: StateFlow<SchoolCalendarConfigEntity?> = configRepo.get()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val events: StateFlow<List<SchoolEventEntity>> = eventRepo.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    fun clearImportStatus() {
        _importStatus.value = null
    }

    fun saveConfig(schoolYear: String, startMonth: Int, endMonth: Int) {
        viewModelScope.launch {
            val newConfig = SchoolCalendarConfigEntity(
                schoolYear = schoolYear,
                startMonth = startMonth,
                endMonth = endMonth
            )
            configRepo.save(newConfig)

            // Auto-configure default Academic Periods to satisfy "Setup Required" check
            // We calculate 4 equal quarters
            val startYear = schoolYear.split("-")[0].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val endYearVal = schoolYear.split("-")[1].toIntOrNull() ?: (startYear + 1)

            val startCal = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, startYear)
                set(Calendar.MONTH, startMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            // Handle end month being in next year if index is smaller (e.g. Start Aug(8), End May(5))
            val actualEndYear = if (endMonth < startMonth) endYearVal else startYear
            val endCal = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, actualEndYear)
                set(Calendar.MONTH, endMonth - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            val totalDuration = endCal.timeInMillis - startCal.timeInMillis
            val quarterDuration = totalDuration / 4

            val q1End = startCal.timeInMillis + quarterDuration
            val q2Start = q1End + 86400000 // +1 day roughly
            val q2End = q2Start + quarterDuration
            val q3Start = q2End + 86400000
            val q3End = q3Start + quarterDuration
            val q4Start = q3End + 86400000
            val q4End = endCal.timeInMillis // align with end

            val existingPeriod = periodRepo.getPeriod()
            if (existingPeriod == null) {
                val defaultPeriod = SchoolPeriodEntity(
                    id = 1,
                    schoolYear = schoolYear,
                    q1Start = startCal.timeInMillis,
                    q1End = q1End,
                    q2Start = q2Start,
                    q2End = q2End,
                    q3Start = q3Start,
                    q3End = q3End,
                    q4Start = q4Start,
                    q4End = q4End,
                    // Apply same to SHS for now
                    shsQ1Start = startCal.timeInMillis, shsQ1End = q1End,
                    shsQ2Start = q2Start, shsQ2End = q2End,
                    shsQ3Start = q3Start, shsQ3End = q3End,
                    shsQ4Start = q4Start, shsQ4End = q4End,
                    synced = false
                )
                periodRepo.insert(defaultPeriod)
            }
        }
    }

    fun addEvent(title: String, description: String, date: Date) {
        viewModelScope.launch {
            val newEvent = SchoolEventEntity(
                date = date.time,
                title = title,
                description = description,
                type = "activity", // Default type
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

    fun importCalendarFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _importStatus.value = "Importing..."
            }
            try {
                // Clear existing events before importing new ones to prevent duplication
                eventRepo.deleteAllEvents()

                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val eventsToInsert = mutableListOf<SchoolEventEntity>()
                
                // Try different date formats
                val formats = listOf(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                )
                
                // Track start/end quarter dates if found in CSV
                var q1Start = 0L; var q1End = 0L
                var q2Start = 0L; var q2End = 0L
                var q3Start = 0L; var q3End = 0L
                var q4Start = 0L; var q4End = 0L

                reader.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        val trimmedLine = line.trim()
                        // Skip header or empty lines
                        if (index == 0 && (trimmedLine.contains("Date", ignoreCase = true) || trimmedLine.contains("Title", ignoreCase = true))) {
                            return@forEachIndexed
                        }
                        if (trimmedLine.isBlank()) return@forEachIndexed

                        val tokens = trimmedLine.split(",")
                        if (tokens.size >= 3) {
                            val dateStr = tokens[0].trim()
                            val title = tokens[1].trim()
                            val description = tokens[2].trim()
                            val type = if (tokens.size > 3 && tokens[3].isNotBlank()) tokens[3].trim() else "activity"
                            
                            var date = 0L
                            for (format in formats) {
                                try {
                                    date = format.parse(dateStr)?.time ?: 0L
                                    if (date > 0) break
                                } catch (e: Exception) {
                                    // Try next format
                                }
                            }

                            if (date > 0 && title.isNotBlank()) {
                                // Check if this event defines a period
                                val lowerTitle = title.lowercase(Locale.getDefault())
                                val isPeriodEvent = type.equals("period", ignoreCase = true) || lowerTitle.contains("quarter") || lowerTitle.contains("grading")

                                if (isPeriodEvent) {
                                    // Logic for capturing quarter dates
                                    when {
                                        (lowerTitle.contains("1st") || lowerTitle.contains("first")) && (lowerTitle.contains("start") || lowerTitle.contains("begin")) -> q1Start = date
                                        (lowerTitle.contains("1st") || lowerTitle.contains("first")) && (lowerTitle.contains("end") || lowerTitle.contains("exam")) -> q1End = date
                                        
                                        (lowerTitle.contains("2nd") || lowerTitle.contains("second")) && (lowerTitle.contains("start") || lowerTitle.contains("begin")) -> q2Start = date
                                        (lowerTitle.contains("2nd") || lowerTitle.contains("second")) && (lowerTitle.contains("end") || lowerTitle.contains("exam")) -> q2End = date
                                        
                                        (lowerTitle.contains("3rd") || lowerTitle.contains("third")) && (lowerTitle.contains("start") || lowerTitle.contains("begin")) -> q3Start = date
                                        (lowerTitle.contains("3rd") || lowerTitle.contains("third")) && (lowerTitle.contains("end") || lowerTitle.contains("exam")) -> q3End = date
                                        
                                        (lowerTitle.contains("4th") || lowerTitle.contains("fourth")) && (lowerTitle.contains("start") || lowerTitle.contains("begin")) -> q4Start = date
                                        (lowerTitle.contains("4th") || lowerTitle.contains("fourth")) && (lowerTitle.contains("end") || lowerTitle.contains("exam")) -> q4End = date
                                    }
                                } 
                                
                                // Always add as an event for visualization, even if it's a period marker
                                val event = SchoolEventEntity(
                                    date = date,
                                    title = title,
                                    description = description,
                                    type = type,
                                    isNoClass = (type.equals("holiday", ignoreCase = true) || type.equals("no-class", ignoreCase = true)),
                                    synced = false
                                )
                                eventsToInsert.add(event)
                            }
                        }
                    }
                }

                if (eventsToInsert.isNotEmpty()) {
                    eventsToInsert.forEach { eventRepo.addEvent(it) }
                }
                
                // If periods were found, update the SchoolPeriodEntity
                if (q1Start > 0 || q1End > 0 || q2Start > 0 || q2End > 0 || q3Start > 0 || q3End > 0 || q4Start > 0 || q4End > 0) {
                    val currentPeriod = periodRepo.getPeriod() ?: SchoolPeriodEntity(
                        id = 1,
                        schoolYear = "2024-2025", 
                        q1Start = 0L, q1End = 0L, q2Start = 0L, q2End = 0L, q3Start = 0L, q3End = 0L, q4Start = 0L, q4End = 0L
                    )
                    
                    val updatedPeriod = currentPeriod.copy(
                        q1Start = if (q1Start > 0) q1Start else currentPeriod.q1Start,
                        q1End = if (q1End > 0) q1End else currentPeriod.q1End,
                        q2Start = if (q2Start > 0) q2Start else currentPeriod.q2Start,
                        q2End = if (q2End > 0) q2End else currentPeriod.q2End,
                        q3Start = if (q3Start > 0) q3Start else currentPeriod.q3Start,
                        q3End = if (q3End > 0) q3End else currentPeriod.q3End,
                        q4Start = if (q4Start > 0) q4Start else currentPeriod.q4Start,
                        q4End = if (q4End > 0) q4End else currentPeriod.q4End,
                        // Apply changes to SHS as well if they are 0
                        shsQ1Start = if (currentPeriod.shsQ1Start == 0L && q1Start > 0) q1Start else currentPeriod.shsQ1Start,
                        shsQ1End = if (currentPeriod.shsQ1End == 0L && q1End > 0) q1End else currentPeriod.shsQ1End,
                        shsQ2Start = if (currentPeriod.shsQ2Start == 0L && q2Start > 0) q2Start else currentPeriod.shsQ2Start,
                        shsQ2End = if (currentPeriod.shsQ2End == 0L && q2End > 0) q2End else currentPeriod.shsQ2End,
                        shsQ3Start = if (currentPeriod.shsQ3Start == 0L && q3Start > 0) q3Start else currentPeriod.shsQ3Start,
                        shsQ3End = if (currentPeriod.shsQ3End == 0L && q3End > 0) q3End else currentPeriod.shsQ3End,
                        shsQ4Start = if (currentPeriod.shsQ4Start == 0L && q4Start > 0) q4Start else currentPeriod.shsQ4Start,
                        shsQ4End = if (currentPeriod.shsQ4End == 0L && q4End > 0) q4End else currentPeriod.shsQ4End
                    )
                    periodRepo.insert(updatedPeriod)
                }

                withContext(Dispatchers.Main) {
                    val msg = if (q1Start > 0 || q1End > 0) {
                        "Imported ${eventsToInsert.size} events & Updated Academic Periods"
                    } else {
                        "Success: Imported ${eventsToInsert.size} events."
                    }
                    _importStatus.value = msg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                 withContext(Dispatchers.Main) {
                    _importStatus.value = "Import Failed: ${e.message}"
                }
            }
        }
    }
}
