package com.attendifyplus.data.local.dao

import androidx.room.*
import com.attendifyplus.data.local.entities.SubjectClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectClassDao {
    @Query("SELECT * FROM subject_classes WHERE teacherId = :teacherId")
    fun getClassesForTeacher(teacherId: String): Flow<List<SubjectClassEntity>>

    @Query("SELECT * FROM subject_classes WHERE gradeLevel = :grade AND section = :section")
    fun getClassesByGradeAndSection(grade: String, section: String): Flow<List<SubjectClassEntity>>

    @Query("SELECT * FROM subject_classes WHERE gradeLevel = :grade AND trackAndStrand = :track")
    suspend fun getClassesByGradeAndTrack(grade: String, track: String): List<SubjectClassEntity>

    @Query("SELECT * FROM subject_classes")
    fun getAllClasses(): List<SubjectClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subjectClass: SubjectClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<SubjectClassEntity>)

    @Delete
    suspend fun delete(subjectClass: SubjectClassEntity)

    @Query("DELETE FROM subject_classes")
    suspend fun deleteAll()
}
