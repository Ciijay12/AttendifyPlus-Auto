package com.attendifyplus.data.local.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "school_periods")
data class SchoolPeriodEntity(
    @PrimaryKey val id: Int = 1, // Singleton or Year ID
    val schoolYear: String,      // e.g., "2023-2024"
    
    // JHS (Junior High School) - Standard Fields
    val q1Start: Long,
    val q1End: Long,
    
    val q2Start: Long,
    val q2End: Long,
    
    val q3Start: Long,
    val q3End: Long,
    
    val q4Start: Long,
    val q4End: Long,

    // SHS (Senior High School) - New Fields
    val shsQ1Start: Long = 0,
    val shsQ1End: Long = 0,
    
    val shsQ2Start: Long = 0,
    val shsQ2End: Long = 0,
    
    val shsQ3Start: Long = 0,
    val shsQ3End: Long = 0,
    
    val shsQ4Start: Long = 0,
    val shsQ4End: Long = 0,
    
    val synced: Boolean = false // Added for synchronization
) {
    @Ignore
    var currentPeriod: String = ""

    fun determineCurrentPeriod(): String {
        val now = System.currentTimeMillis()
        return when {
            // Check JHS Dates
            now in q1Start..q1End -> "1st Quarter"
            now in q2Start..q2End -> "2nd Quarter"
            now in q3Start..q3End -> "3rd Quarter"
            now in q4Start..q4End -> "4th Quarter"
            
            // Check SHS Dates (Fallback if JHS doesn't match, or distinct if non-overlapping)
            now in shsQ1Start..shsQ1End -> "1st Quarter" // SHS usually aligns or just differs by Sem
            now in shsQ2Start..shsQ2End -> "2nd Quarter"
            now in shsQ3Start..shsQ3End -> "3rd Quarter"
            now in shsQ4Start..shsQ4End -> "4th Quarter"
            
            // Semester Logic for SHS (if needed, can be derived)
            // For now, returning Quarter name is standard.
            
            else -> ""
        }
    }
}
