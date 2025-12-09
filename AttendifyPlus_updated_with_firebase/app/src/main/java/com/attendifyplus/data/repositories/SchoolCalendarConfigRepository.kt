package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SchoolCalendarConfigDao
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity

class SchoolCalendarConfigRepository(private val dao: SchoolCalendarConfigDao) {
    fun get() = dao.get()
    suspend fun save(config: SchoolCalendarConfigEntity) = dao.insert(config)
}
