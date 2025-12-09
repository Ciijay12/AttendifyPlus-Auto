package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_calendar_config")
data class SchoolCalendarConfigEntity(
    @PrimaryKey val id: Int = 1, // Singleton ID
    val schoolYear: String,      // e.g., "2024-2025"
    val startMonth: Int,         // Month number (1-12)
    val endMonth: Int            // Month number (1-12)
)
