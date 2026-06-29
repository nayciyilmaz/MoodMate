package com.example.moodmate.domain.model

data class ProfileUiState(
    val shouldNavigateToLogin: Boolean = false,
    val notificationEnabled: Boolean = true,
    val selectedLanguage: String = "Türkçe",
    val showLogoutDialog: Boolean = false
)