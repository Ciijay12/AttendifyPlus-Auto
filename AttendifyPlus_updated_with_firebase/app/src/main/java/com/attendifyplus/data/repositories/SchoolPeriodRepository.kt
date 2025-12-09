package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SchoolPeriodDao
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SchoolPeriodRepository(private val dao: SchoolPeriodDao) {
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
    
    suspend fun insert(period: SchoolPeriodEntity) = dao.insert(period)
}
