package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_classes")
data class SubjectClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: String, // Links class to a specific teacher
    val subjectName: String, // e.g., "Mathematics"
    val gradeLevel: String,  // e.g., "Grade 9"
    val section: String,      // e.g., "A"
    val startTime: String? = null, // e.g., "08:00" - Removed usage in UI, kept in DB for now if needed for sorting
    val endTime: String? = null,    // e.g., "09:30"
    val classDays: String? = null,   // e.g., "Monday,Wednesday,Friday"
    val trackAndStrand: String? = null // For SHS, e.g., "STEM", "ABM"
)
