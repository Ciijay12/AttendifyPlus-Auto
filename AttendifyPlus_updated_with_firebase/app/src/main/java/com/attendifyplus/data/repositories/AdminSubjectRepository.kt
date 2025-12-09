package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.AdminSubjectDao
import com.attendifyplus.data.local.entities.AdminSubjectEntity
import kotlinx.coroutines.flow.Flow

class AdminSubjectRepository(private val dao: AdminSubjectDao) {
    fun getAllSubjectsFlow(): Flow<List<AdminSubjectEntity>> = dao.getAllSubjectsFlow()
    suspend fun insert(subject: AdminSubjectEntity) = dao.insert(subject)
    suspend fun insertAll(subjects: List<AdminSubjectEntity>) = dao.insertAll(subjects)
    suspend fun update(subject: AdminSubjectEntity) = dao.update(subject)
    suspend fun delete(subject: AdminSubjectEntity) = dao.delete(subject)
    suspend fun deleteAll() = dao.deleteAll()
}
