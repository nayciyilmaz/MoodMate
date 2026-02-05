package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    private val _selectedTheme = MutableStateFlow("Açık Tema")
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _notificationEnabled = MutableStateFlow("Açık")
    val notificationEnabled: StateFlow<String> = _notificationEnabled.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Türkçe")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setTheme(theme: String) {
        _selectedTheme.value = theme
    }

    fun setNotification(status: String) {
        _notificationEnabled.value = status
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearUser()
            _shouldNavigateToLogin.value = true
        }
    }

    fun resetNavigationFlag() {
        _shouldNavigateToLogin.value = false
    }
}