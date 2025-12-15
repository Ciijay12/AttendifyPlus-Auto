package com.attendifyplus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.repositories.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class DebugSettingsViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebugUiState>(DebugUiState.Idle)
    val uiState: StateFlow<DebugUiState> = _uiState

    // Ensure we have an active Firebase session (Anonymous for Admin)
    private suspend fun ensureAuth() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
                Timber.d("Admin auto-authenticated for debug action")
            } catch (e: Exception) {
                Timber.e(e, "Failed to auto-authenticate")
            }
        }
    }

    fun clearRemoteData() {
        viewModelScope.launch {
            _uiState.value = DebugUiState.Loading
            try {
                ensureAuth()
                syncRepository.clearRemoteData()
                _uiState.value = DebugUiState.Success("Remote data cleared successfully")
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = DebugUiState.Error("Failed to clear remote data: ${e.message}")
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            _uiState.value = DebugUiState.Loading
            try {
                syncRepository.clearLocalData()
                _uiState.value = DebugUiState.Success("Local data cleared successfully")
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = DebugUiState.Error("Failed to clear local data: ${e.message}")
            }
        }
    }

    fun performFactoryReset() {
        viewModelScope.launch {
            _uiState.value = DebugUiState.Loading
            try {
                syncRepository.performFactoryReset()
                _uiState.value = DebugUiState.Success("Local app reset successfully. Restart app.")
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = DebugUiState.Error("Failed to reset app: ${e.message}")
            }
        }
    }
    
    // New: Nuke Everything (Remote + Local)
    fun nukeAllData() {
        viewModelScope.launch {
            _uiState.value = DebugUiState.Loading
            try {
                ensureAuth()
                
                // 1. Clear Remote First (so we don't sync it back)
                syncRepository.clearRemoteData()
                
                // 2. Clear Local
                syncRepository.performFactoryReset()
                
                _uiState.value = DebugUiState.Success("COMPLETE RESET: Server and Device wiped. Restart App.")
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = DebugUiState.Error("Nuke failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = DebugUiState.Idle
    }
}

sealed class DebugUiState {
    object Idle : DebugUiState()
    object Loading : DebugUiState()
    data class Success(val message: String) : DebugUiState()
    data class Error(val message: String) : DebugUiState()
}
