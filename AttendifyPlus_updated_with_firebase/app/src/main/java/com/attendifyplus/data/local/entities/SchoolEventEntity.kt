package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_events")
data class SchoolEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = 0L, // Timestamp of the event (midnight)
    val title: String = "",
    val type: String = "", // "holiday", "suspension", "event"
    val description: String? = null,
    val isNoClass: Boolean = false, // If true, attendance is not required
    val synced: Boolean = false
)
