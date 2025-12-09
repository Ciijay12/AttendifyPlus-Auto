package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val timestamp: Long,
    val status: String,
    val synced: Boolean = false,
    
    // New fields for Adviser vs Subject Teacher
    val type: String = "homeroom", // "homeroom" or "subject"
    val subject: String? = null,    // e.g., "Math", "Science" (null if homeroom)
    val academicPeriod: String? = null // e.g., "Q1", "Q2"
)
