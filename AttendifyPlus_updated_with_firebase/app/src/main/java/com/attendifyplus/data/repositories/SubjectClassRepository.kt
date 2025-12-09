package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SubjectClassDao
import com.attendifyplus.data.local.entities.SubjectClassEntity
import kotlinx.coroutines.flow.Flow

class SubjectClassRepository(private val dao: SubjectClassDao) {
    fun getClassesForTeacher(teacherId: String) = dao.getClassesForTeacher(teacherId)
    
    fun getClassesByGradeAndSection(grade: String, section: String): Flow<List<SubjectClassEntity>> {
        return dao.getClassesByGradeAndSection(grade, section)
    }

    suspend fun getClassesByGradeAndTrack(grade: String, track: String): List<SubjectClassEntity> {
        return dao.getClassesByGradeAndTrack(grade, track)
    }

    suspend fun insert(subjectClass: SubjectClassEntity) = dao.insert(subjectClass)
    suspend fun delete(subjectClass: SubjectClassEntity) = dao.delete(subjectClass)
    suspend fun deleteAll() = dao.deleteAll()
}
