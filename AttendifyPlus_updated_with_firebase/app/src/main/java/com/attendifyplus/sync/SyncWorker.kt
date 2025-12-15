package com.attendifyplus.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.repositories.*
import com.attendifyplus.util.NotificationHelper
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Two-way sync with Firebase Realtime Database.
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params), KoinComponent {
    
    private val attendanceRepo: AttendanceRepository by inject()
    private val studentRepo: StudentRepository by inject()
    private val teacherRepo: TeacherRepository by inject()
    private val periodRepo: SchoolPeriodRepository by inject()
    private val eventRepo: SchoolEventRepository by inject()
    private val subjectClassRepo: SubjectClassRepository by inject()
    private val notificationHelper: NotificationHelper by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        notificationHelper.showSyncNotification("Sync Started", "Synchronizing data...")
        try {
            val db = FirebaseDatabase.getInstance().reference

            // --- 1. SYNC ALL REMOTE DATA TO LOCAL ---
            // These methods pull all data from Firebase and update the local Room DB.
            teacherRepo.syncAll()
            studentRepo.syncAll()
            eventRepo.syncEvents()
            periodRepo.syncPeriod()
            subjectClassRepo.syncAll() // Added subject class sync

            // --- 2. PUSH UNSYNCED LOCAL DATA TO REMOTE ---
            // This part handles data that was created offline and needs to be pushed up.
            
            // Push Unsynced Attendance
            val unsynced = attendanceRepo.getUnsynced()
            val attendanceRef = db.child("attendance")
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
                attendanceRef.push().setValue(map).await()
                attendanceRepo.markSynced(listOf(local.id))
            }
            
            // Note: For other entities like Student/Teacher, we force-push all local data.
            // This is a simple but effective strategy for the "Force Sync" button.
            val localTeachers = teacherRepo.getAll()
            if (localTeachers.isNotEmpty()) {
                val teachersMap = localTeachers.associate { it.id to it }
                db.child("teachers").updateChildren(teachersMap).await()
            }
            
            val localStudents = studentRepo.getAllList()
            if (localStudents.isNotEmpty()) {
                 val studentsMap = localStudents.associate { it.id to it }
                 db.child("students").updateChildren(studentsMap).await()
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
