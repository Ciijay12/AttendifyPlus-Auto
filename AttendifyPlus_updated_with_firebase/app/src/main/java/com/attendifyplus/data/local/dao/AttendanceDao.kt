package com.attendifyplus.data.local.dao

import androidx.room.*
import com.attendifyplus.data.local.entities.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance WHERE synced = 0")
    suspend fun getUnsynced(): List<AttendanceEntity>

    @Query("UPDATE attendance SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
    
    @Query("SELECT COUNT(*) FROM attendance WHERE synced = 0")
    fun countUnsynced(): Flow<Int>

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteByStudentId(studentId: String)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getHistoryByStudent(studentId: String): Flow<List<AttendanceEntity>>

    // History for a specific subject class
    @Query("SELECT * FROM attendance WHERE subject = :subjectName ORDER BY timestamp DESC")
    fun getHistoryBySubject(subjectName: String): Flow<List<AttendanceEntity>>

    // History for homeroom (advisory)
    @Query("SELECT * FROM attendance WHERE type = 'homeroom' ORDER BY timestamp DESC")
    fun getHomeroomHistory(): Flow<List<AttendanceEntity>>

    // Get all history for export
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<AttendanceEntity>

    // Filtered history for export (Date Range only)
    @Query("SELECT * FROM attendance WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getAllHistoryInDateRange(startDate: Long, endDate: Long): List<AttendanceEntity>

    // Filtered history for export (Date Range + Subject)
    @Query("SELECT * FROM attendance WHERE subject = :subjectName AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getHistoryBySubjectInDateRange(subjectName: String, startDate: Long, endDate: Long): List<AttendanceEntity>
    
    // Filtered history for export (Date Range + List of Subjects) - NEW for "Export All" restricted to assigned subjects
    @Query("SELECT * FROM attendance WHERE subject IN (:subjectNames) AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getHistoryBySubjectsInDateRange(subjectNames: List<String>, startDate: Long, endDate: Long): List<AttendanceEntity>

    // Filtered history for export (Date Range + Homeroom)
    @Query("SELECT * FROM attendance WHERE type = 'homeroom' AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getHomeroomHistoryInDateRange(startDate: Long, endDate: Long): List<AttendanceEntity>

    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'present' AND timestamp >= :timestamp")
    fun countPresentSince(timestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'late' AND timestamp >= :timestamp")
    fun countLateSince(timestamp: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'absent' AND timestamp >= :timestamp")
    fun countAbsentSince(timestamp: Long): Flow<Int>

    // Subject specific counts (Range based) - Updated
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'present' AND subject = :subject AND timestamp BETWEEN :start AND :end")
    fun countPresentBetweenForSubject(subject: String, start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'late' AND subject = :subject AND timestamp BETWEEN :start AND :end")
    fun countLateBetweenForSubject(subject: String, start: Long, end: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'absent' AND subject = :subject AND timestamp BETWEEN :start AND :end")
    fun countAbsentBetweenForSubjectNew(subject: String, start: Long, end: Long): Flow<Int>

    // Legacy "Since" helpers (can be replaced by range with end=MAX_LONG, but keeping for backward compat if needed)
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'present' AND timestamp >= :timestamp AND subject = :subject")
    fun countPresentSinceForSubject(timestamp: Long, subject: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'late' AND timestamp >= :timestamp AND subject = :subject")
    fun countLateSinceForSubject(timestamp: Long, subject: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM attendance WHERE status = 'absent' AND timestamp >= :timestamp AND subject = :subject")
    fun countAbsentSinceForSubject(timestamp: Long, subject: String): Flow<Int>

    // Helper to check existence for sync
    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND timestamp = :timestamp")
    suspend fun countByStudentAndTimestamp(studentId: String, timestamp: Long): Int
}
