package com.attendifyplus.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.data.repositories.AdminRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.TeacherRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

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
    private val adminRepo: AdminRepository,
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
            
            // Check Session Validity for Admin
            if (role == "admin") {
                viewModelScope.launch {
                    val isValid = adminRepo.checkSessionValidity()
                    if (isValid) {
                        _isLoggedIn.value = true
                        _userRole.value = role
                        _userId.value = id
                        
                        // Restore Firebase Auth for Admin if missing (e.g. after app restart)
                        try {
                            val auth = FirebaseAuth.getInstance()
                            if (auth.currentUser == null) {
                                auth.signInAnonymously().await()
                                Timber.d("Restored Admin Firebase Session")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to restore admin firebase session")
                        }
                    } else {
                        // Force Logout if session is invalid
                        onLogout()
                    }
                }
            } else {
                _isLoggedIn.value = true
                _userRole.value = role
                _userId.value = id
            }
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
        // If admin, clear remote session
        if (_userRole.value == "admin") {
            viewModelScope.launch {
                adminRepo.logoutAdmin()
            }
        }
        
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userRole.value = null
        _userId.value = null
        
        // Also sign out from Firebase
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Timber.e(e, "Error signing out from Firebase")
        }
    }

    fun loginTeacher(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            // Admin Check
            // Use AdminRepository to check credentials
            if (user == adminRepo.getUsername() && pass == adminRepo.getPassword()) {
                try {
                    // Authenticate as Admin (Anonymous for now to satisfy auth!=null rules)
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        auth.signInAnonymously().await()
                        Timber.d("Admin signed in anonymously to Firebase")
                    }
                    
                    // Claim Session
                    adminRepo.loginAdmin()
                    
                } catch (e: Exception) {
                    Timber.e(e, "Failed to authenticate admin with Firebase")
                    // We continue anyway, as local login might be sufficient for some features
                    // But for session management, we still try to claim session
                }

                delay(500)
                saveSession("admin", "admin")
                _loginState.value = LoginState.Success("admin", "admin")
                return@launch
            }

            val teacher = teacherRepo.getByUsername(user, forceRemote = true)
            if (teacher != null) {
                if (teacher.password == pass) {
                    // BUG FIX: Force remote fetch to get latest credential status for teacher
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
            
            val student = studentRepo.findByLogin(user, forceRemote = true)

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
    
    // Admin Helper to update local credentials
    fun updateAdminCredentials(user: String, pass: String) {
         viewModelScope.launch {
             adminRepo.updateCredentials(user, pass)
         }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
