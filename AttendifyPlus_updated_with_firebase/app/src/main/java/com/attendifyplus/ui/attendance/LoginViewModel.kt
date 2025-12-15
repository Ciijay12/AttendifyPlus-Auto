package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.TeacherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val role: String, val id: String) : LoginState
    data class FirstTimeLogin(val role: String, val id: String) : LoginState
    data class Error(val message: String) : LoginState
}

class LoginViewModel(
    private val teacherRepo: TeacherRepository,
    private val studentRepo: StudentRepository,
    private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Session State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val prefs = context.getSharedPreferences("attendify_session", Context.MODE_PRIVATE)

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        val loggedIn = prefs.getBoolean("is_logged_in", false)
        val role = prefs.getString("user_role", null)
        val id = prefs.getString("user_id", null)
        
        if (loggedIn && role != null) {
            _isLoggedIn.value = true
            _userRole.value = role
            _userId.value = id
        } else {
            _isLoggedIn.value = false
            _userRole.value = null
            _userId.value = null
        }
    }

    private fun saveSession(role: String, id: String) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_role", role)
            .putString("user_id", id)
            .apply()
        
        _isLoggedIn.value = true
        _userRole.value = role
        _userId.value = id
    }

    fun onLogout() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userRole.value = null
        _userId.value = null
    }

    fun loginTeacher(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            // Admin Check (Hardcoded for now as in most examples, or fetched from DB if seeded)
            if (user == "admin" && pass == "admin123") {
                delay(500)
                saveSession("admin", "admin")
                _loginState.value = LoginState.Success("admin", "admin")
                return@launch
            }

            val teacher = teacherRepo.getByUsername(user)
            if (teacher != null) {
                if (teacher.password == pass) {
                    // BUG FIX: Force remote fetch to get latest credential status for teacher
                    // We use getById because getByUsername might be slower or redundant if we have the ID
                    // But wait, we used username to login. Let's use getById with forceRemote now that we have the ID.
                    val freshTeacher = teacherRepo.getById(teacher.id, forceRemote = true) ?: teacher

                    if (!freshTeacher.hasChangedCredentials) {
                        _loginState.value = LoginState.FirstTimeLogin("teacher", freshTeacher.id)
                    } else {
                        saveSession("teacher", freshTeacher.id)
                        _loginState.value = LoginState.Success("teacher", freshTeacher.id)
                    }
                } else {
                    _loginState.value = LoginState.Error("Invalid password")
                }
            } else {
                _loginState.value = LoginState.Error("User not found")
            }
        }
    }

    fun loginStudent(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val student = studentRepo.findByLogin(user)

            if (student != null) {
                val storedPass = student.password ?: "123456"
                if (storedPass == pass) {
                    // BUG FIX: Force remote fetch to get latest credential status
                    val freshStudent = studentRepo.getById(student.id, forceRemote = true) ?: student
                    
                     if (!freshStudent.hasChangedCredentials) {
                        _loginState.value = LoginState.FirstTimeLogin("student", freshStudent.id)
                    } else {
                        saveSession("student", freshStudent.id)
                        _loginState.value = LoginState.Success("student", freshStudent.id)
                    }
                } else {
                    _loginState.value = LoginState.Error("Invalid password")
                }
            } else {
                _loginState.value = LoginState.Error("Student not found")
            }
        }
    }
    
    fun updateCredentials(role: String, id: String, newUser: String, newPass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                if (role == "teacher") {
                    teacherRepo.updateCredentials(id, newUser, newPass)
                    saveSession("teacher", id)
                    _loginState.value = LoginState.Success("teacher", id)
                } else if (role == "student") {
                    studentRepo.updateCredentials(id, newUser, newPass)
                    saveSession("student", id)
                    _loginState.value = LoginState.Success("student", id)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Failed to update credentials: ${e.message}")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
