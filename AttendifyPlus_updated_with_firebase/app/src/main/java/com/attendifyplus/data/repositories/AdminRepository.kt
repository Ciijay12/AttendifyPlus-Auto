package com.attendifyplus.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class AdminRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
    private val dbRef = FirebaseDatabase.getInstance().getReference("admin")
    
    // Unique Session ID generated on login
    private var currentSessionId: String? = null

    // Local Credentials
    fun getUsername(): String = prefs.getString("admin_username", "admin") ?: "admin"
    fun getPassword(): String = prefs.getString("admin_password", "admin123") ?: "admin123"

    fun updateCredentials(username: String, pass: String) {
        prefs.edit()
            .putString("admin_username", username)
            .putString("admin_password", pass)
            .apply()
    }
    
    // Remote Session Management
    // We store an "activeSessionId" in Firebase.
    // When logging in, we generate a new ID, push it to Firebase.
    // If another device logs in, they overwrite this ID.
    // The previous device will check this ID periodically (or on critical actions) and logout if it mismatch.
    
    suspend fun loginAdmin(): String {
        val newSessionId = UUID.randomUUID().toString()
        currentSessionId = newSessionId
        
        try {
            dbRef.child("activeSessionId").setValue(newSessionId).await()
            // Store locally too for recovery
            prefs.edit().putString("session_id", newSessionId).apply()
        } catch (e: Exception) {
            Timber.e(e, "Failed to set active session ID on Firebase")
        }
        
        return newSessionId
    }
    
    suspend fun checkSessionValidity(): Boolean {
        // If we don't have a current session (e.g. app restart), load from prefs
        if (currentSessionId == null) {
            currentSessionId = prefs.getString("session_id", null)
        }
        
        if (currentSessionId == null) return false // No active session
        
        try {
            val snapshot = dbRef.child("activeSessionId").get().await()
            if (snapshot.exists()) {
                val remoteSessionId = snapshot.getValue(String::class.java)
                return remoteSessionId == currentSessionId
            } else {
                // If node doesn't exist, we assume we are valid and claim it
                // Or return true? Let's claim it to be safe.
                dbRef.child("activeSessionId").setValue(currentSessionId).await()
                return true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check session validity")
            return true // Fail open to avoid locking out due to network error, or fail closed?
            // Fail open is better for UX, but worse for security.
            // Given offline capability, we might assume valid if network fails.
        }
    }
    
    suspend fun logoutAdmin() {
        try {
            // Clear session on remote if we own it
            if (checkSessionValidity()) {
                dbRef.child("activeSessionId").removeValue().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear session on logout")
        }
        currentSessionId = null
        prefs.edit().remove("session_id").apply()
    }
}
