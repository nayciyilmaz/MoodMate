package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.ProfileUiState
import com.example.moodmate.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun setNotification(status: String) {
        _uiState.value = _uiState.value.copy(notificationEnabled = status)
    }

    fun setLanguage(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearUser()
            _uiState.value = _uiState.value.copy(shouldNavigateToLogin = true)
        }
    }

    fun resetNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = false)
    }
}