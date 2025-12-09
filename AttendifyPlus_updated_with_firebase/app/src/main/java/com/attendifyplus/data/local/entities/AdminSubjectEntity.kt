package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_subjects")
data class AdminSubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val gradeLevel: String,
    val semester: String? = null, // "1st", "2nd", or null for JHS/Core
    val track: String? = null, // Specific track if applicable, or null for Core
    val type: String = "Core" // "Core", "Applied", "Specialized"
)
