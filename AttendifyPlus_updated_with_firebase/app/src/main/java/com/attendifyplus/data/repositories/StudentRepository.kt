package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.StudentDao
import com.attendifyplus.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudentRepository(private val dao: StudentDao) {
    fun getAll(): Flow<List<StudentEntity>> = dao.getAll()
    // Helper to get List synchronously (suspending)
    suspend fun getAllList(): List<StudentEntity> = dao.getAll().first()
    
    suspend fun getById(id: String) = dao.getById(id)

    suspend fun getByUsername(username: String) = dao.getByUsername(username)

    suspend fun findByLogin(login: String) = dao.findByLogin(login)

    fun getByClass(grade: String, section: String): Flow<List<StudentEntity>> = dao.getByClass(grade, section)
    
    // New method for synchronous list fetching
    suspend fun getByClassList(grade: String, section: String): List<StudentEntity> = dao.getByClassList(grade, section)

    // Get all students in a class, including archived
    suspend fun getAllByClass(grade: String, section: String): List<StudentEntity> = dao.getAllByClass(grade, section)

    // Archive methods
    fun getArchivedStudents(): Flow<List<StudentEntity>> = dao.getArchivedStudents()
    suspend fun archive(id: String) = dao.archive(id)
    suspend fun restore(id: String) = dao.restore(id)

    suspend fun insert(student: StudentEntity) = dao.insert(student)
    suspend fun insertAll(students: List<StudentEntity>) = dao.insertAll(students)
    suspend fun update(student: StudentEntity) = dao.update(student)
    suspend fun updateCredentials(studentId: String, username: String, password: String) = dao.updateCredentials(studentId, username, password)
    suspend fun delete(id: String) = dao.delete(id)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun countByClass(grade: String, section: String) = dao.countByClass(grade, section)
}
