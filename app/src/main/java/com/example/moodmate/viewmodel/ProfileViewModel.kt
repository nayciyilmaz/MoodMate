package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.ProfileUiState
import com.example.moodmate.local.TokenManager
import com.example.moodmate.notification.NotificationPreferenceHelper
import com.example.moodmate.notification.NotificationScheduler
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val notificationScheduler: NotificationScheduler,
    private val moodRepository: MoodRepository,
    private val adviceRepository: AdviceRepository,
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
        Timber.d("Ayarlar yüklendi: dil=$languageName, bildirim=$isEnabled")
        _uiState.value = _uiState.value.copy(
            selectedLanguage = languageName,
            notificationEnabled = isEnabled
        )
    }

    fun setNotification(enabled: Boolean) {
        Timber.d("Bildirim ayarı değiştiriliyor: $enabled")
        NotificationPreferenceHelper.setNotificationEnabled(context, enabled)
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)
        if (enabled) {
            notificationScheduler.scheduleDailyNotifications()
            Timber.d("Bildirimler planlandı")
        } else {
            notificationScheduler.cancelAllNotifications()
            Timber.d("Bildirimler iptal edildi")
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
            Timber.d("Dil değiştiriliyor: $currentLanguageCode -> $languageCode")
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
            Timber.d("Çıkış yapılıyor")
            moodRepository.clearAllMoodsForUser()
            adviceRepository.clearAdviceForUser()
            tokenManager.clearUser()
            Timber.d("Çıkış tamamlandı, login ekranına yönlendiriliyor")
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