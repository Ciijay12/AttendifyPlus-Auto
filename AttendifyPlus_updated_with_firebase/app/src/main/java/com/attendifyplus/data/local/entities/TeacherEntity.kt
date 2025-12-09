package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String, // Unique Teacher Code
    val username: String,
    val password: String = "123456", // Default password for migration/testing
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String = "subject", // "adviser" or "subject"
    
    // Classification
    val department: String = "JHS", // "JHS" (Junior High) or "SHS" (Senior High)
    
    // New fields for Advisory Class details
    val advisoryGrade: String? = null,
    val advisorySection: String? = null,
    val advisoryStartTime: String? = null, // Format "HH:mm" (24-hour)
    val advisoryTrack: String? = null, // Added for SHS Strand/Track
    
    // Credentials Management
    val hasChangedCredentials: Boolean = false
)
