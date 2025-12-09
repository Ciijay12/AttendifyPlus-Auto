package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SchoolEventDao
import com.attendifyplus.data.local.entities.SchoolEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SchoolEventRepository(private val dao: SchoolEventDao) {
    fun getAllEvents(): Flow<List<SchoolEventEntity>> = dao.getAllEvents()
    
    suspend fun addEvent(event: SchoolEventEntity) = dao.insert(event)
    
    suspend fun insertAll(events: List<SchoolEventEntity>) = dao.insertAll(events)
    
    suspend fun deleteEvent(id: Int) = dao.delete(id)
    
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
}
