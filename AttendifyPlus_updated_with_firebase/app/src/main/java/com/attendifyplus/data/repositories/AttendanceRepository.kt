package com.attendifyplus.data.repositories

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.attendifyplus.data.local.dao.AttendanceDao
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.sync.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AttendanceRepository(private val dao: AttendanceDao, private val context: Context) {
    suspend fun record(entity: AttendanceEntity) = dao.insert(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteByStudentId(studentId: String) = dao.deleteByStudentId(studentId)
    suspend fun getUnsynced() = dao.getUnsynced()
    suspend fun markSynced(ids: List<Long>) = dao.markSynced(ids)
    
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun insertAll(entities: List<AttendanceEntity>) = dao.insertAll(entities)

    // Expose flow of unsynced count
    val unsyncedCount = dao.countUnsynced()

    fun getStudentHistory(studentId: String): Flow<List<AttendanceEntity>> = dao.getHistoryByStudent(studentId)
    
    fun getClassHistory(subjectName: String?): Flow<List<AttendanceEntity>> {
        return if (subjectName != null) {
            dao.getHistoryBySubject(subjectName)
        } else {
            dao.getHomeroomHistory()
        }
    }

    suspend fun getAllHistory(): List<AttendanceEntity> {
        return dao.getAllHistory()
    }

    suspend fun getAllHistoryInDateRange(startDate: Long, endDate: Long): List<AttendanceEntity> {
        return dao.getAllHistoryInDateRange(startDate, endDate)
    }

    suspend fun getHistoryBySubjectInDateRange(subjectName: String, startDate: Long, endDate: Long): List<AttendanceEntity> {
        return dao.getHistoryBySubjectInDateRange(subjectName, startDate, endDate)
    }
    
    suspend fun getHistoryBySubjectsInDateRange(subjectNames: List<String>, startDate: Long, endDate: Long): List<AttendanceEntity> {
        return dao.getHistoryBySubjectsInDateRange(subjectNames, startDate, endDate)
    }

    suspend fun getHomeroomHistoryInDateRange(startDate: Long, endDate: Long): List<AttendanceEntity> {
        return dao.getHomeroomHistoryInDateRange(startDate, endDate)
    }

    fun getPresentCount(since: Long) = dao.countPresentSince(since)
    fun getLateCount(since: Long) = dao.countLateSince(since)
    fun getAbsentCount(since: Long) = dao.countAbsentSince(since)

    // Subject specific counts
    fun getPresentCountForSubject(since: Long, subject: String) = dao.countPresentSinceForSubject(since, subject)
    fun getLateCountForSubject(since: Long, subject: String) = dao.countLateSinceForSubject(since, subject)
    fun getAbsentCountForSubject(since: Long, subject: String) = dao.countAbsentSinceForSubject(since, subject)

    // New Range-Based Subject Counts for Smart Dashboard
    fun getPresentCountBetweenForSubject(subject: String, start: Long, end: Long) = dao.countPresentBetweenForSubject(subject, start, end)
    fun getLateCountBetweenForSubject(subject: String, start: Long, end: Long) = dao.countLateBetweenForSubject(subject, start, end)
    fun getAbsentCountBetweenForSubject(subject: String, start: Long, end: Long) = dao.countAbsentBetweenForSubjectNew(subject, start, end)

    // Sync helper
    suspend fun exists(studentId: String, timestamp: Long): Boolean {
        return dao.countByStudentAndTimestamp(studentId, timestamp) > 0
    }

    // Last Sync Timestamp Management
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong("last_sync_timestamp", 0L))
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

    fun updateLastSyncTimestamp() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("last_sync_timestamp", now).apply()
        _lastSyncTimestamp.value = now
    }

    /**
     * Triggers an immediate synchronization with Firebase.
     */
    fun triggerSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
