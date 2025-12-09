package com.attendifyplus.data.local.dao

import androidx.room.*
import com.attendifyplus.data.local.entities.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getById(id: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE id = :id")
    fun getByIdFlow(id: String): Flow<TeacherEntity?>

    @Query("SELECT * FROM teachers WHERE username = :username")
    suspend fun getByUsername(username: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teacher: TeacherEntity)
    
    @Query("SELECT * FROM teachers")
    suspend fun getAll(): List<TeacherEntity>

    @Query("SELECT * FROM teachers")
    fun getAllFlow(): Flow<List<TeacherEntity>>

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Query("UPDATE teachers SET advisoryGrade = :grade, advisorySection = :section, advisoryStartTime = :startTime WHERE id = :teacherId")
    suspend fun updateAdvisoryDetails(teacherId: String, grade: String?, section: String?, startTime: String?)

    @Query("UPDATE teachers SET username = :username, password = :password, hasChangedCredentials = 1 WHERE id = :teacherId")
    suspend fun updateCredentials(teacherId: String, username: String, password: String)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM teachers")
    suspend fun deleteAll()
}
