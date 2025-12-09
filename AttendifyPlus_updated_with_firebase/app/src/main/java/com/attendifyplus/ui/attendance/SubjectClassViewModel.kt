package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.data.repositories.SubjectClassRepository
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClassSectionOption(
    val grade: String,
    val section: String,
    val track: String?
)

class SubjectClassViewModel(
    private val repository: SubjectClassRepository,
    private val teacherRepository: TeacherRepository,
    private val teacherId: String = "T001" // In real app, get from session
) : ViewModel() {

    val classes: StateFlow<List<SubjectClassEntity>> = repository.getClassesForTeacher(teacherId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fetch all teachers who are advisers to get available sections configured by Admin
    val availableSections: StateFlow<List<ClassSectionOption>> = teacherRepository.getAllFlow()
        .map { teachers ->
            teachers.filter { 
                !it.advisoryGrade.isNullOrBlank() && !it.advisorySection.isNullOrBlank() 
            }.map { 
                ClassSectionOption(it.advisoryGrade!!, it.advisorySection!!, it.advisoryTrack)
            }.sortedWith(compareBy({ it.grade.toIntOrNull() ?: 99 }, { it.section }))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addClass(subject: String, grade: String, section: String, startTime: String?, endTime: String?, trackAndStrand: String?) {
        viewModelScope.launch {
            val classEntity = SubjectClassEntity(
                teacherId = teacherId,
                subjectName = subject,
                gradeLevel = grade,
                section = section,
                startTime = startTime,
                endTime = endTime,
                trackAndStrand = if (grade == "11" || grade == "12") trackAndStrand else null
            )
            repository.insert(classEntity)
        }
    }

    fun updateClass(id: Long, subject: String, grade: String, section: String, startTime: String?, endTime: String?, trackAndStrand: String?) {
        viewModelScope.launch {
            val classEntity = SubjectClassEntity(
                id = id,
                teacherId = teacherId,
                subjectName = subject,
                gradeLevel = grade,
                section = section,
                startTime = startTime,
                endTime = endTime,
                trackAndStrand = if (grade == "11" || grade == "12") trackAndStrand else null
            )
            repository.insert(classEntity)
        }
    }

    fun deleteClass(subjectClass: SubjectClassEntity) {
        viewModelScope.launch {
            repository.delete(subjectClass)
        }
    }
}
