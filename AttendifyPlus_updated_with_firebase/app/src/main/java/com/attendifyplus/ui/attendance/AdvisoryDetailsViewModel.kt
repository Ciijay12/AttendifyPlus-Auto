package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.TeacherEntity
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdvisoryDetailsViewModel(
    private val teacherRepo: TeacherRepository,
    private val context: Context, // Added Context for prefs
    private val teacherId: String = "T001" // TODO: Get from Session
) : ViewModel() {

    private val _teacher = MutableStateFlow<TeacherEntity?>(null)
    val teacher: StateFlow<TeacherEntity?> = _teacher

    private val prefs = context.getSharedPreferences("school_config", Context.MODE_PRIVATE)

    // Persistent Enabled Tracks
    private val _enabledTracks = MutableStateFlow(loadEnabledTracks())
    val enabledTracks: StateFlow<Set<String>> = _enabledTracks.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTeacher(teacherId)
    }

    fun loadTeacher(id: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            teacherRepo.getByIdFlow(id).collect { 
                _teacher.value = it
            }
        }
    }

    private fun loadEnabledTracks(): Set<String> {
        return prefs.getStringSet("enabled_tracks", setOf("ABM", "HUMSS", "STEM", "GAS", "AFA", "HE", "IA", "ICT", "Arts and Design", "Sports"))?.toSet() ?: emptySet()
    }

    fun toggleTrack(track: String, enabled: Boolean) {
        val current = _enabledTracks.value.toMutableSet()
        if (enabled) current.add(track) else current.remove(track)
        _enabledTracks.value = current
        prefs.edit().putStringSet("enabled_tracks", current).apply()
    }

    fun saveDetails(grade: String, section: String, startTime: String?, track: String?) {
        val currentTeacher = _teacher.value
        
        val updatedTeacher = if (currentTeacher != null) {
            currentTeacher.copy(
                advisoryGrade = grade,
                advisorySection = section,
                advisoryStartTime = startTime,
                advisoryTrack = track, // Save track
                role = "adviser" // Always set to adviser when they have advisory class
            )
        } else {
            // This case should ideally not happen if the UI is driven by an existing teacher.
            // But as a fallback, create a new entity. This needs more details for a real user.
            TeacherEntity(
                id = teacherId,
                username = "new.teacher",
                password = "123456", // Default password
                firstName = "New",
                lastName = "Teacher",
                email = null,
                role = "adviser",
                advisoryGrade = grade,
                advisorySection = section,
                advisoryStartTime = startTime,
                advisoryTrack = track
            )
        }
        
        viewModelScope.launch {
            teacherRepo.insert(updatedTeacher)
        }
    }

    fun deleteClass() {
        _teacher.value?.let {
            val updated = it.copy(
                advisoryGrade = null,
                advisorySection = null,
                advisoryStartTime = null,
                advisoryTrack = null,
                // Revert role to subject teacher if they no longer advise a class
                role = "subject" 
            )
            viewModelScope.launch {
                teacherRepo.insert(updated)
            }
        }
    }
}
