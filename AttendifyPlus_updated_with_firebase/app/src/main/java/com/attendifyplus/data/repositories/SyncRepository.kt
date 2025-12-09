package com.attendifyplus.data.repositories

import android.content.Context
import com.attendifyplus.data.SessionManager
import com.attendifyplus.data.local.AttendifyDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository specifically for handling sync operations and data clearing
 */
class SyncRepository(
    private val database: AttendifyDatabase,
    private val context: Context
) {
    private val db = FirebaseDatabase.getInstance().reference

    /**
     * Clears all data from Firebase Realtime Database
     * Specifically targeting: /attendance, /students, /config
     */
    suspend fun clearRemoteData() {
        try {
            db.child("attendance").removeValue().await()
            db.child("students").removeValue().await()
            db.child("config").removeValue().await()
            Timber.d("Remote data cleared successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear remote data")
            throw e
        }
    }

    /**
     * Clears local database tables related to sync
     */
    suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        Timber.d("Local data cleared successfully")
    }

    /**
     * Wipes all app data to simulate a fresh install
     * - Clears Database
     * - Clears Shared Preferences
     * - Resets SessionManager
     */
    suspend fun performFactoryReset() {
        withContext(Dispatchers.IO) {
            // 1. Clear Database
            database.clearAllTables()

            // 2. Clear Shared Preferences
            val prefsToClear = listOf("school_config", "attendify_session", "app_prefs")
            prefsToClear.forEach { prefName ->
                context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().commit()
            }

            // 3. Reset Session Manager (In-memory)
            SessionManager.currentStudentId = "S001-DEMO"
            SessionManager.currentTeacherId = "T001"
        }
        Timber.d("Factory reset completed")
    }
}
