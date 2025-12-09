package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.TeacherDao
import com.attendifyplus.data.local.entities.TeacherEntity
import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val dao: TeacherDao) {
    suspend fun getById(id: String) = dao.getById(id)
    fun getByIdFlow(id: String): Flow<TeacherEntity?> = dao.getByIdFlow(id)
    suspend fun getByUsername(username: String) = dao.getByUsername(username)
    suspend fun insert(teacher: TeacherEntity) = dao.insert(teacher)
    suspend fun getAll() = dao.getAll()
    fun getAllFlow() = dao.getAllFlow()
    suspend fun update(teacher: TeacherEntity) = dao.update(teacher)
    suspend fun delete(id: String) = dao.delete(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun updateAdvisoryDetails(teacherId: String, grade: String?, section: String?, startTime: String?) = dao.updateAdvisoryDetails(teacherId, grade, section, startTime)
    suspend fun updateCredentials(teacherId: String, username: String, password: String) = dao.updateCredentials(teacherId, username, password)
}
