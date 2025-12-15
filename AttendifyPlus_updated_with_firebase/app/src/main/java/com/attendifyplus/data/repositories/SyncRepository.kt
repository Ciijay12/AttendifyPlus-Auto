package com.attendifyplus.data.repositories

import android.content.Context
import com.attendifyplus.data.SessionManager
import com.attendifyplus.data.local.AttendifyDatabase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

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
     * Specifically targeting all operational nodes: 
     * /attendance, /students, /teachers, /subjectClasses, /config
     */
    suspend fun clearRemoteData() {
        try {
            // Remove all major nodes to ensure complete reset
            val nodesToRemove = listOf(
                "attendance",
                "students",
                "teachers",
                "subjectClasses",
                "config" // Includes schoolPeriod and events
            )
            
            nodesToRemove.forEach { node ->
                db.child(node).removeValue().await()
            }
            
            Timber.d("Remote data cleared successfully (All Nodes)")
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
     * - Clears ALL Shared Preferences (Robustly finding files)
     * - Resets SessionManager
     */
    suspend fun performFactoryReset() {
        withContext(Dispatchers.IO) {
            // 1. Clear Database
            database.clearAllTables()

            // 2. Clear ALL Shared Preferences
            // Robust way: List files in shared_prefs directory and clear them
            try {
                val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                if (prefsDir.exists() && prefsDir.isDirectory) {
                    prefsDir.list()?.forEach { fileName ->
                        // Strip .xml extension to get preference name
                        val prefName = fileName.replace(".xml", "")
                        context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().commit()
                    }
                } else {
                    // Fallback to known prefs if directory logic fails (e.g. strict strict mode or strange OS behavior)
                    val knownPrefs = listOf("school_config", "attendify_session", "app_prefs")
                    knownPrefs.forEach { prefName ->
                        context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().commit()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error clearing shared preferences")
                // Fallback attempt
                val knownPrefs = listOf("school_config", "attendify_session", "app_prefs")
                knownPrefs.forEach { prefName ->
                    context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().commit()
                }
            }

            // 3. Reset Session Manager (In-memory)
            SessionManager.currentStudentId = "S001-DEMO"
            SessionManager.currentTeacherId = "T001"
        }
        Timber.d("Factory reset completed")
    }
}
