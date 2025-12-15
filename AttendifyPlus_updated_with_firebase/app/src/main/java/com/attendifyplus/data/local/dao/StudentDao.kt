package com.attendifyplus.data.local.dao

import androidx.room.*
import com.attendifyplus.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAll(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: String): StudentEntity?
    
    // Added Flow version of getById
    @Query("SELECT * FROM students WHERE id = :id")
    fun getByIdFlow(id: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE username = :username")
    suspend fun getByUsername(username: String): StudentEntity?

    @Query("SELECT * FROM students WHERE id = :login OR username = :login OR firstName = :login")
    suspend fun findByLogin(login: String): StudentEntity?

    // Updated to exclude archived students
    @Query("SELECT * FROM students WHERE grade = :grade AND section = :section AND isArchived = 0 ORDER BY lastName ASC")
    fun getByClass(grade: String, section: String): Flow<List<StudentEntity>>
    
    @Query("SELECT * FROM students WHERE grade = :grade AND section = :section AND isArchived = 0 ORDER BY lastName ASC")
    suspend fun getByClassList(grade: String, section: String): List<StudentEntity>

    // New: Get all students in a class, including archived, for export purposes
    @Query("SELECT * FROM students WHERE grade = :grade AND section = :section ORDER BY lastName ASC")
    suspend fun getAllByClass(grade: String, section: String): List<StudentEntity>

    // New: Get Archived Students
    @Query("SELECT * FROM students WHERE isArchived = 1 ORDER BY lastName ASC")
    fun getArchivedStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Query("UPDATE students SET username = :username, password = :password, hasChangedCredentials = 1 WHERE id = :studentId")
    suspend fun updateCredentials(studentId: String, username: String, password: String)
    
    // New: Archive and Restore methods
    @Query("UPDATE students SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("UPDATE students SET isArchived = 0 WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM students")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM students WHERE grade = :grade AND section = :section AND isArchived = 0")
    suspend fun countByClass(grade: String, section: String): Int
}
