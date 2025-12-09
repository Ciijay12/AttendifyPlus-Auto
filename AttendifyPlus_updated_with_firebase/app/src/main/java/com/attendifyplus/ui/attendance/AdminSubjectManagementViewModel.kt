package com.attendifyplus.ui.attendance

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.AdminSubjectEntity
import com.attendifyplus.data.repositories.AdminSubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class AdminSubjectManagementViewModel(
    private val repository: AdminSubjectRepository,
    private val context: Context
) : ViewModel() {

    val subjects: StateFlow<List<AdminSubjectEntity>> = repository.getAllSubjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importStatus = MutableStateFlow<String?>(null)
    val importStatus: StateFlow<String?> = _importStatus.asStateFlow()

    fun clearImportStatus() {
        _importStatus.value = null
    }

    fun addSubject(subject: AdminSubjectEntity) {
        viewModelScope.launch {
            repository.insert(subject)
        }
    }

    fun updateSubject(subject: AdminSubjectEntity) {
        viewModelScope.launch {
            repository.update(subject)
        }
    }

    fun deleteSubject(subject: AdminSubjectEntity) {
        viewModelScope.launch {
            repository.delete(subject)
        }
    }

    fun importSubjectsFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
             _importStatus.value = "Importing..."
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val subjectsToInsert = mutableListOf<AdminSubjectEntity>()

                reader.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        val trimmedLine = line.trim()
                         // Basic Header Check or skip empty
                        if (index == 0 && (trimmedLine.contains("Subject Name", ignoreCase = true) || trimmedLine.contains("Grade Level", ignoreCase = true))) {
                            return@forEachIndexed
                        }
                        if (trimmedLine.isBlank()) return@forEachIndexed

                        val tokens = trimmedLine.split(",")
                        if (tokens.size >= 2) {
                            val subjectName = tokens[0].trim()
                            val gradeLevel = tokens[1].trim()
                            
                            // Optional fields
                            val semester = if (tokens.size > 2 && tokens[2].isNotBlank()) tokens[2].trim() else null
                            val track = if (tokens.size > 3 && tokens[3].isNotBlank()) tokens[3].trim() else null
                            val type = if (tokens.size > 4 && tokens[4].isNotBlank()) tokens[4].trim() else "Core"

                            if (subjectName.isNotBlank() && gradeLevel.isNotBlank()) {
                                val subject = AdminSubjectEntity(
                                    subjectName = subjectName,
                                    gradeLevel = gradeLevel,
                                    semester = semester,
                                    track = track,
                                    type = type
                                )
                                subjectsToInsert.add(subject)
                            }
                        }
                    }
                }

                if (subjectsToInsert.isNotEmpty()) {
                    repository.insertAll(subjectsToInsert)
                    _importStatus.value = "Success: Imported ${subjectsToInsert.size} subjects."
                } else {
                    _importStatus.value = "No valid subject records found in CSV."
                }
            } catch (e: Exception) {
                _importStatus.value = "Import Failed: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}
