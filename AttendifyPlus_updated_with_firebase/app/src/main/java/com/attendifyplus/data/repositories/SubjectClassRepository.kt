package com.attendifyplus.data.repositories

import com.attendifyplus.data.local.dao.SubjectClassDao
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SubjectClassRepository(private val dao: SubjectClassDao) {
    
    private val dbRef = FirebaseDatabase.getInstance().getReference("subjectClasses")

    fun getClassesForTeacher(teacherId: String) = dao.getClassesForTeacher(teacherId)
    
    fun getClassesByGradeAndSection(grade: String, section: String): Flow<List<SubjectClassEntity>> {
        return dao.getClassesByGradeAndSection(grade, section)
    }
    
    fun getAllClassesFlow(): Flow<List<SubjectClassEntity>> = dao.getAllClassesFlow()

    suspend fun getClassesByGradeAndTrack(grade: String, track: String): List<SubjectClassEntity> {
        return dao.getClassesByGradeAndTrack(grade, track)
    }

    suspend fun insert(subjectClass: SubjectClassEntity) {
        dao.insert(subjectClass)
        try {
            // Use a composite key for uniqueness in Firebase
            // Include teacherId to avoid collision if multiple teachers have same Subject/Grade/Section (rare but possible)
            val key = "${subjectClass.teacherId}_${subjectClass.subjectName}_${subjectClass.gradeLevel}_${subjectClass.section}"
                .replace(".", "")
                .replace("#", "")
                .replace("$", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "_")
            
            dbRef.child(key).setValue(subjectClass).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to push subject class to Firebase")
        }
    }

    suspend fun delete(subjectClass: SubjectClassEntity) {
        dao.delete(subjectClass)
        try {
            val key = "${subjectClass.teacherId}_${subjectClass.subjectName}_${subjectClass.gradeLevel}_${subjectClass.section}"
                .replace(".", "")
                .replace("#", "")
                .replace("$", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "_")
            
            dbRef.child(key).removeValue().await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete subject class from Firebase")
        }
    }

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun syncAll() {
        try {
            val snapshot = dbRef.get().await()
            val remoteClasses = mutableListOf<SubjectClassEntity>()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val subjectClass = child.getValue(SubjectClassEntity::class.java)
                    if (subjectClass != null) {
                        remoteClasses.add(subjectClass)
                    }
                }
            }
            
            // Smart Sync: Mirror remote state (Add/Update and DELETE missing)
            val localClasses = dao.getAllClasses()
            
            // 1. Update/Insert Remote Classes
            remoteClasses.forEach { remote ->
                val match = localClasses.find { local ->
                    local.teacherId == remote.teacherId &&
                    local.subjectName == remote.subjectName &&
                    local.gradeLevel == remote.gradeLevel &&
                    local.section == remote.section
                }
                
                if (match != null) {
                    // Update existing, preserving local ID
                    dao.insert(remote.copy(id = match.id))
                } else {
                    // Insert new, letting Room gen ID (id=0)
                    dao.insert(remote.copy(id = 0))
                }
            }
            
            // 2. Delete Local Classes that are MISSING from Remote
            localClasses.forEach { local ->
                val existsRemote = remoteClasses.any { remote ->
                    remote.teacherId == local.teacherId &&
                    remote.subjectName == local.subjectName &&
                    remote.gradeLevel == local.gradeLevel &&
                    remote.section == local.section
                }
                
                if (!existsRemote) {
                    dao.delete(local)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync subject classes from Firebase")
        }
    }
}
