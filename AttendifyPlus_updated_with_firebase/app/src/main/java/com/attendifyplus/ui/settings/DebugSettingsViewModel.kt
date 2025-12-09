package com.attendifyplus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendifyplus.data.repositories.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DebugSettingsViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DebugUiState>(DebugUiState.Idle)
    val uiState: StateFlow<DebugUiState> = _uiState

    fun clearRemoteData() {
        viewModelScope.launch {
            _uiState.value = DebugUiState.Loading
            try {
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
                _uiState.value = DebugUiState.Success("App reset successfully. Please restart the app.")
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = DebugUiState.Error("Failed to reset app: ${e.message}")
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
