package com.attendifyplus.sync

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.util.NotificationHelper
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*

/**
 * Two-way sync with Firebase Realtime Database.
 * Strategy: last-write-wins based on `updatedAt` (epoch millis).
 * Records pushed under /attendance/{schoolId}/{generatedId}
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params), KoinComponent {
    
    // Koin injection in Worker - Enabled
    private val attendanceRepo: AttendanceRepository by inject()
    private val studentRepo: StudentRepository by inject()
    private val periodRepo: SchoolPeriodRepository by inject()
    private val notificationHelper: NotificationHelper by inject()

    @Suppress("UNCHECKED_CAST")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        notificationHelper.showSyncNotification("Sync Started", "Synchronizing attendance data...")
        try {
            val db = FirebaseDatabase.getInstance().reference
            val attendanceRef = db.child("attendance")
            val studentsRef = db.child("students")
            val configRef = db.child("config")

            // --- 1. SYNC ATTENDANCE ---
            val unsynced = attendanceRepo.getUnsynced()
            for (local in unsynced) {
                val map = mapOf(
                    "studentId" to local.studentId,
                    "timestamp" to local.timestamp,
                    "status" to local.status,
                    "type" to local.type,
                    "subject" to (local.subject ?: ""),
                    "updatedAt" to System.currentTimeMillis(),
                    "deviceId" to android.os.Build.MODEL
                )
                val newRef = attendanceRef.push()
                newRef.setValue(map).await()
                attendanceRepo.markSynced(listOf(local.id))
            }

            // Pull remote attendance
            val snapshot = attendanceRef.get().await()
            if (snapshot.exists()) {
                var pulledCount = 0
                for (child in snapshot.children) {
                    val remote = child.value as? Map<String, Any> ?: continue
                    val studentId = remote["studentId"] as? String ?: continue
                    val timestamp = (remote["timestamp"] as? Number)?.toLong() ?: continue
                    val status = remote["status"] as? String ?: "present"
                    val type = remote["type"] as? String ?: "homeroom"
                    val subjectRaw = remote["subject"] as? String ?: ""
                    val subject = if (subjectRaw.isBlank()) null else subjectRaw
                    
                    if (!attendanceRepo.exists(studentId, timestamp)) {
                        attendanceRepo.record(
                            AttendanceEntity(
                                studentId = studentId,
                                timestamp = timestamp,
                                status = status,
                                type = type,
                                subject = subject,
                                synced = true 
                            )
                        )
                        pulledCount++
                    }
                }
                if (pulledCount > 0) {
                    Timber.d("Synced $pulledCount attendance records from remote.")
                }
            }

            // --- 2. SYNC STUDENTS ---
            val studentsSnap = studentsRef.get().await()
            if (studentsSnap.exists()) {
                val remoteStudents = mutableListOf<StudentEntity>()
                for (child in studentsSnap.children) {
                    val remote = child.value as? Map<String, Any> ?: continue
                    val remoteId = remote["id"] as? String ?: child.key ?: continue
                    val first = remote["firstName"] as? String ?: ""
                    val last = remote["lastName"] as? String ?: ""
                    val grade = remote["grade"] as? String ?: ""
                    val section = remote["section"] as? String ?: ""
                    
                    remoteStudents.add(
                        StudentEntity(
                            id = remoteId,
                            firstName = first,
                            lastName = last,
                            grade = grade,
                            section = section
                        )
                    )
                }
                if (remoteStudents.isNotEmpty()) {
                    studentRepo.insertAll(remoteStudents)
                }
            }

            // --- 3. SYNC SCHOOL PERIOD CONFIG ---
            val localPeriod = periodRepo.getPeriod()
            
            // If local period exists and is NOT synced, push to cloud
            if (localPeriod != null && !localPeriod.synced) {
                val periodMap = mapOf(
                    "schoolYear" to localPeriod.schoolYear,
                    "q1Start" to localPeriod.q1Start, "q1End" to localPeriod.q1End,
                    "q2Start" to localPeriod.q2Start, "q2End" to localPeriod.q2End,
                    "q3Start" to localPeriod.q3Start, "q3End" to localPeriod.q3End,
                    "q4Start" to localPeriod.q4Start, "q4End" to localPeriod.q4End,
                    "updatedAt" to System.currentTimeMillis()
                )
                configRef.child("schoolPeriod").setValue(periodMap).await()
                // Mark local as synced (re-insert with synced=true)
                periodRepo.insert(localPeriod.copy(synced = true))
            }

            // Pull remote config and update local if newer or missing
            val remoteConfigSnap = configRef.child("schoolPeriod").get().await()
            if (remoteConfigSnap.exists()) {
                val remote = remoteConfigSnap.value as? Map<String, Any>
                if (remote != null) {
                    val year = remote["schoolYear"] as? String ?: ""
                    val q1s = (remote["q1Start"] as? Number)?.toLong() ?: 0L
                    val q1e = (remote["q1End"] as? Number)?.toLong() ?: 0L
                    val q2s = (remote["q2Start"] as? Number)?.toLong() ?: 0L
                    val q2e = (remote["q2End"] as? Number)?.toLong() ?: 0L
                    val q3s = (remote["q3Start"] as? Number)?.toLong() ?: 0L
                    val q3e = (remote["q3End"] as? Number)?.toLong() ?: 0L
                    val q4s = (remote["q4Start"] as? Number)?.toLong() ?: 0L
                    val q4e = (remote["q4End"] as? Number)?.toLong() ?: 0L
                    
                    // Compare? For simplicity, overwrite local if it exists or create new
                    // In a real conflict resolution, check timestamps. Here we assume Admin is single source of truth.
                    val newEntity = SchoolPeriodEntity(
                        id = 1, // Singleton
                        schoolYear = year,
                        q1Start = q1s, q1End = q1e,
                        q2Start = q2s, q2End = q2e,
                        q3Start = q3s, q3End = q3e,
                        q4Start = q4s, q4End = q4e,
                        synced = true
                    )
                    periodRepo.insert(newEntity)
                }
            }

            // Update last sync timestamp
            attendanceRepo.updateLastSyncTimestamp()

            notificationHelper.showSyncNotification("Sync Completed", "All data is up to date.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            notificationHelper.showSyncNotification("Sync Failed", "Could not sync data. Check connection.", isError = true)
            Result.retry()
        }
    }
}
