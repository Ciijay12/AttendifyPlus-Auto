package com.attendifyplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.attendifyplus.data.local.entities.SchoolEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolEventDao {
    @Query("SELECT * FROM school_events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<SchoolEventEntity>>

    @Query("SELECT * FROM school_events WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getEventsBetween(start: Long, end: Long): Flow<List<SchoolEventEntity>>

    @Query("SELECT * FROM school_events WHERE date BETWEEN :startTime AND :endTime ORDER BY date ASC")
    fun getEventsForTimeRange(startTime: Long, endTime: Long): Flow<List<SchoolEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SchoolEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SchoolEventEntity>)

    @Query("DELETE FROM school_events WHERE id = :id")
    suspend fun delete(id: Int)
    
    @Query("SELECT COUNT(*) FROM school_events WHERE date = :date AND isNoClass = 1")
    suspend fun isNoClassDay(date: Long): Int

    @Query("DELETE FROM school_events")
    suspend fun deleteAll()
}
