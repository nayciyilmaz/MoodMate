package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.ProfileUiState
import com.example.moodmate.local.TokenManager
import com.example.moodmate.notification.NotificationScheduler
import com.example.moodmate.util.LocaleHelper
import com.example.moodmate.util.NotificationPreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val notificationScheduler: NotificationScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _shouldRecreateActivity = MutableStateFlow(false)
    val shouldRecreateActivity: StateFlow<Boolean> = _shouldRecreateActivity.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val currentLanguageCode = LocaleHelper.getLanguage(context)
        val languageName = when (currentLanguageCode) {
            "tr" -> "Türkçe"
            "en" -> "English"
            "es" -> "Español"
            "it" -> "Italiano"
            else -> "Türkçe"
        }

        val isEnabled = NotificationPreferenceHelper.isNotificationEnabled(context)

        _uiState.value = _uiState.value.copy(
            selectedLanguage = languageName,
            notificationEnabled = isEnabled
        )
    }

    fun setNotification(enabled: Boolean) {
        NotificationPreferenceHelper.setNotificationEnabled(context, enabled)
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)

        if (enabled) {
            notificationScheduler.scheduleDailyNotifications()
        } else {
            notificationScheduler.cancelAllNotifications()
        }
    }

    fun setLanguage(language: String) {
        val languageCode = when (language) {
            "Türkçe" -> "tr"
            "English" -> "en"
            "Español" -> "es"
            "Italiano" -> "it"
            else -> "tr"
        }

        val currentLanguageCode = LocaleHelper.getLanguage(context)
        if (languageCode != currentLanguageCode) {
            LocaleHelper.saveLanguage(context, languageCode)
            _uiState.value = _uiState.value.copy(selectedLanguage = language)
            _shouldRecreateActivity.value = true
        }
    }

    fun resetRecreateFlag() {
        _shouldRecreateActivity.value = false
    }

    fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearUser()
            _uiState.value = _uiState.value.copy(
                showLogoutDialog = false,
                shouldNavigateToLogin = true
            )
        }
    }

    fun resetNavigationFlag() {
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = false)
    }
}