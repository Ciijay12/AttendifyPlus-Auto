package com.attendifyplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolPeriodDao {
    @Query("SELECT * FROM school_periods LIMIT 1")
    fun getPeriodFlow(): Flow<SchoolPeriodEntity?>

    @Query("SELECT * FROM school_periods LIMIT 1")
    suspend fun getPeriod(): SchoolPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(period: SchoolPeriodEntity)

    @Query("DELETE FROM school_periods")
    suspend fun deleteAll()
}
