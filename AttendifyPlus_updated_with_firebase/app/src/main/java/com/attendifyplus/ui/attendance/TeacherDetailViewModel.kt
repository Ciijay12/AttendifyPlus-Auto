package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.data.repositories.SubjectClassRepository
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherDetailViewModel(
    private val teacherRepo: TeacherRepository,
    private val subjectRepo: SubjectClassRepository
) : ViewModel() {

    private val _teacher = MutableStateFlow<TeacherEntity?>(null)
    val teacher: StateFlow<TeacherEntity?> = _teacher.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectClassEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectClassEntity>> = _subjects.asStateFlow()

    fun loadTeacher(id: String) {
        viewModelScope.launch {
            teacherRepo.getByIdFlow(id).collect { t ->
                _teacher.value = t
            }
        }
        viewModelScope.launch {
            subjectRepo.getClassesForTeacher(id).collect { list ->
                _subjects.value = list
            }
        }
    }

    fun updateProfile(username: String, first: String, last: String, email: String) {
        val current = _teacher.value ?: return
        val updated = current.copy(username = username, firstName = first, lastName = last, email = email)
        viewModelScope.launch {
            teacherRepo.insert(updated)
        }
    }

    // Feature: Admin Password Reset
    fun resetPassword(newPassword: String) {
        val current = _teacher.value ?: return
        val updated = current.copy(password = newPassword)
        viewModelScope.launch {
            teacherRepo.insert(updated)
        }
    }

    fun updateAdvisory(grade: String, section: String) {
        val t = _teacher.value ?: return
        viewModelScope.launch {
             // IMPORTANT: When assigning advisory, we MUST set role to "adviser"
             // We also should preserve existing data if any, but updateAdvisoryDetails might handle nulls.
             // Here we update the whole entity to be safe about the role change.
             
             val updated = t.copy(
                 role = "adviser",
                 advisoryGrade = grade,
                 advisorySection = section
             )
             teacherRepo.insert(updated)
        }
    }

    fun removeAdvisory() {
        val t = _teacher.value ?: return
        viewModelScope.launch {
            // When removing advisory, revert role to "subject"
            val updated = t.copy(
                role = "subject",
                advisoryGrade = null,
                advisorySection = null,
                advisoryStartTime = null
            )
            teacherRepo.insert(updated)
        }
    }

    fun addSubject(subjectName: String, grade: String, section: String, sched: String) {
        val t = _teacher.value ?: return
        viewModelScope.launch {
            val newClass = SubjectClassEntity(
                // id is auto-generated Long, so we skip it (default is 0)
                teacherId = t.id,
                subjectName = subjectName,
                gradeLevel = grade,
                section = section,
                startTime = sched
            )
            subjectRepo.insert(newClass)
        }
    }

    fun deleteSubject(subject: SubjectClassEntity) {
        viewModelScope.launch {
            subjectRepo.delete(subject)
        }
    }
}
