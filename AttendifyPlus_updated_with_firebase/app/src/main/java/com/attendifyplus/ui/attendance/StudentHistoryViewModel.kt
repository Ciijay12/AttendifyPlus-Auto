package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.AttendanceEntity
import com.attendifyplus.data.local.entities.SubjectClassEntity
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.SubjectClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudentHistoryViewModel(
    private val attendanceRepo: AttendanceRepository,
    private val studentRepo: StudentRepository,
    private val subjectClassRepo: SubjectClassRepository,
    private val studentId: String
) : ViewModel() {

    // 1. Raw History
    private val _rawHistory = attendanceRepo.getStudentHistory(studentId)

    // 2. Student's Enrolled Subjects
    private val _enrolledSubjects = MutableStateFlow<List<SubjectClassEntity>>(emptyList())
    val enrolledSubjects: StateFlow<List<SubjectClassEntity>> = _enrolledSubjects.asStateFlow()

    // 3. Selected Subject Filter (null = All)
    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    // 4. Filtered History
    val history: StateFlow<List<AttendanceEntity>> = combine(
        _rawHistory,
        _selectedSubject
    ) { history, subjectFilter ->
        if (subjectFilter == null) {
            history
        } else {
            history.filter { it.subject == subjectFilter }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadEnrolledSubjects()
    }

    private fun loadEnrolledSubjects() {
        viewModelScope.launch {
            val student = studentRepo.getById(studentId)
            if (student != null) {
                // Determine enrolled subjects based on Grade/Section match
                subjectClassRepo.getAllClassesFlow().collect { allClasses ->
                    val filtered = allClasses.filter { cls ->
                        val sGrade = student.grade.filter { it.isDigit() }
                        val cGrade = cls.gradeLevel.filter { it.isDigit() }
                        val sSection = student.section.trim().lowercase().replace("section", "").trim()
                        val cSection = cls.section.trim().lowercase().replace("section", "").trim()
                        
                        // Robust match
                        val gradeMatch = (sGrade.isNotEmpty() && cGrade.isNotEmpty() && sGrade == cGrade) || (student.grade == cls.gradeLevel)
                        val sectionMatch = sSection == cSection || sSection.contains(cSection) || cSection.contains(sSection)
                        
                        gradeMatch && sectionMatch
                    }
                    _enrolledSubjects.value = filtered
                }
            }
        }
    }

    fun setSubjectFilter(subjectName: String?) {
        _selectedSubject.value = subjectName
    }
}
