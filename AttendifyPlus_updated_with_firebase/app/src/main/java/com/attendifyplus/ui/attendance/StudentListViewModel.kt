package com.attendifyplus.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Calendar
import java.util.UUID

class StudentListViewModel(private val repo: StudentRepository) : ViewModel() {
    private val _students = MutableStateFlow<List<StudentEntity>>(emptyList())
    val students: StateFlow<List<StudentEntity>> = _students.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            repo.getAll().collect { list ->
                _students.value = list
            }
        }
    }

    fun addOrUpdate(id: String, first: String, last: String, grade: String, section: String) {
        viewModelScope.launch {
            repo.insert(StudentEntity(id = id, firstName = first, lastName = last, grade = grade, section = section))
            // No explicit load needed, Flow updates automatically
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
             repo.delete(id)
             // No explicit load needed
        }
    }
    
    fun generateId(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) % 100
        val random = (1000..9999).random()
        return "I-$year-$random"
    }

    // Reads an input stream from a URI, parses CSV, and inserts students
    fun importCsvFromStream(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val newStudents = mutableListOf<StudentEntity>()
                
                // CSV Parser logic
                // Supported Formats:
                // 1. id, firstName, lastName, grade, section (5 columns)
                // 2. firstName, lastName, grade, section (4 columns -> Auto ID)
                
                var isFirstLine = true
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.isBlank()) return@forEach
                        
                        val tokens = line.split(",").map { it.trim() }
                        
                        // Check header row
                        if (isFirstLine) {
                            isFirstLine = false
                            if (tokens[0].equals("id", ignoreCase = true) || 
                                tokens[0].equals("firstName", ignoreCase = true) ||
                                tokens[0].equals("first name", ignoreCase = true)) {
                                return@forEach
                            }
                        }

                        if (tokens.size >= 5) {
                            // Case 1: ID Provided (or empty first column)
                            val providedId = tokens[0]
                            val finalId = if (providedId.isBlank()) generateId() else providedId
                            
                            val entity = StudentEntity(
                                id = finalId,
                                firstName = tokens[1],
                                lastName = tokens[2],
                                grade = tokens[3],
                                section = tokens[4]
                            )
                            newStudents.add(entity)
                        } else if (tokens.size == 4) {
                            // Case 2: No ID column provided
                            val entity = StudentEntity(
                                id = generateId(),
                                firstName = tokens[0],
                                lastName = tokens[1],
                                grade = tokens[2],
                                section = tokens[3]
                            )
                            newStudents.add(entity)
                        }
                    }
                }
                
                if (newStudents.isNotEmpty()) {
                    repo.insertAll(newStudents)
                    Timber.d("Imported ${newStudents.size} students.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error importing CSV")
            }
        }
    }
    
    fun importCsvSample() {
        // Keep for testing if needed
        viewModelScope.launch {
            val list = listOf(
                StudentEntity(id = "S001", firstName = "Juan", lastName = "Dela Cruz", grade = "10", section = "A"),
                StudentEntity(id = "S002", firstName = "Maria", lastName = "Santos", grade = "10", section = "A"),
                StudentEntity(id = "S003", firstName = "Pedro", lastName = "Reyes", grade = "10", section = "B")
            )
            repo.insertAll(list)
        }
    }
}
