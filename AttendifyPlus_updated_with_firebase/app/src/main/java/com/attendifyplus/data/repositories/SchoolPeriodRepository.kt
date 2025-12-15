package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SchoolPeriodDao
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SchoolPeriodRepository(private val dao: SchoolPeriodDao) {
    
    private val dbRef = FirebaseDatabase.getInstance().getReference("config/schoolPeriod")

    val periodFlow: Flow<SchoolPeriodEntity?> = dao.getPeriodFlow().map { period ->
        period?.apply {
            currentPeriod = determineCurrentPeriod()
        }
    }
    
    suspend fun getPeriod(): SchoolPeriodEntity? {
        val period = dao.getPeriod()
        period?.apply {
            currentPeriod = determineCurrentPeriod()
        }
        return period
    }
    
    suspend fun insert(period: SchoolPeriodEntity) {
        dao.insert(period)
        try {
            dbRef.setValue(period).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to push school period to Firebase")
        }
    }

    suspend fun syncPeriod() {
        try {
            val snapshot = dbRef.get().await()
            if (snapshot.exists()) {
                val remotePeriod = snapshot.getValue(SchoolPeriodEntity::class.java)
                if (remotePeriod != null) {
                    dao.insert(remotePeriod)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync school period from Firebase")
        }
    }
}
