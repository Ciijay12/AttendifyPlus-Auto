package com.attendifyplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolCalendarConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: SchoolCalendarConfigEntity)

    @Query("SELECT * FROM school_calendar_config WHERE id = 1")
    fun get(): Flow<SchoolCalendarConfigEntity?>
}
