package com.example.moodmate.data

data class ProfileUiState(
    val shouldNavigateToLogin: Boolean = false,
    val notificationEnabled: Boolean = true,
    val selectedLanguage: String = "Türkçe",
    val showLogoutDialog: Boolean = false
)