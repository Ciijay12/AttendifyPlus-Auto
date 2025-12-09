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
            now in q1Start..q1End -> "1st Quarter"
            now in q2Start..q2End -> "2nd Quarter"
            now in q3Start..q3End -> "3rd Quarter"
            now in q4Start..q4End -> "4th Quarter"
            // For SHS specific, we might need context or separate logic, 
            // but usually we display general quarter or Semester if needed. 
            // Assuming simplified view for now or overlapping dates.
            else -> ""
        }
    }
}
