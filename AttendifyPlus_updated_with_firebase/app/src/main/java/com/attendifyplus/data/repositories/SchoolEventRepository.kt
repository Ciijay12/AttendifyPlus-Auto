package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SchoolEventDao
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar

class SchoolEventRepository(private val dao: SchoolEventDao) {
    
    private val dbRef = FirebaseDatabase.getInstance().getReference("config/events")

    fun getAllEvents(): Flow<List<SchoolEventEntity>> = dao.getAllEvents()
    
    suspend fun addEvent(event: SchoolEventEntity) {
        dao.insert(event)
        try {
            // We use a generated key for events to avoid conflicts, but need a stable ID if we want to update.
            // Using date as a key might work if we have one event per day, but can be tricky.
            // For now, let's push with auto-ID. This means we can't easily update.
            // A better way is to use a unique ID from the start.
            dbRef.push().setValue(event).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to push event to Firebase")
        }
    }
    
    suspend fun insertAll(events: List<SchoolEventEntity>) {
        dao.insertAll(events)
        try {
            val updates = events.associate { it.date.toString() to it } // Use date as key for simplicity
            dbRef.updateChildren(updates).await()
        } catch(e: Exception) {
            Timber.e(e, "Failed to push events to Firebase")
        }
    }
    
    suspend fun deleteEvent(id: Int) {
        // Deleting from Firebase is hard without a stable key. 
        // This part needs a better sync strategy for deletes.
        dao.delete(id)
    }
    
    suspend fun deleteAllEvents() {
        dao.deleteAll()
        try {
            dbRef.removeValue().await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete remote events")
        }
    }

    suspend fun isNoClass(timestamp: Long): Boolean {
        // Check strictly for that day
        return dao.isNoClassDay(timestamp) > 0
    }

    fun getEventsForMonth(year: Int, month: Int): Flow<List<SchoolEventEntity>> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        val startTime = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val endTime = cal.timeInMillis
        return dao.getEventsForTimeRange(startTime, endTime)
    }
    
    suspend fun syncEvents() {
        try {
            val remoteEvents = mutableListOf<SchoolEventEntity>()
            val snapshot = dbRef.get().await()
            
            // Even if snapshot doesn't exist (empty remote), we should clear local to match
            // But usually we assume remote has data. If remote is empty, we wipe local.
            
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val event = child.getValue(SchoolEventEntity::class.java)
                    if (event != null) {
                        remoteEvents.add(event)
                    }
                }
            }
            
            // Full Sync Strategy: Local state must mirror Remote state
            // 1. Clear local table
            dao.deleteAll()
            
            // 2. Insert fresh data from remote
            if (remoteEvents.isNotEmpty()) {
                dao.insertAll(remoteEvents)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync events from Firebase")
        }
    }
}
