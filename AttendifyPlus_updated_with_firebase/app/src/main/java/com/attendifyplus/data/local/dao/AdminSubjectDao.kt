package com.attendifyplus.data.local.dao

import androidx.room.*
import com.attendifyplus.data.local.entities.AdminSubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminSubjectDao {
    @Query("SELECT * FROM admin_subjects")
    fun getAllSubjectsFlow(): Flow<List<AdminSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: AdminSubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<AdminSubjectEntity>)

    @Update
    suspend fun update(subject: AdminSubjectEntity)

    @Delete
    suspend fun delete(subject: AdminSubjectEntity)
    
    @Query("DELETE FROM admin_subjects")
    suspend fun deleteAll()
}
