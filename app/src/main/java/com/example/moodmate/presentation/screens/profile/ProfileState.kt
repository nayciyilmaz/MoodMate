package com.example.moodmate.presentation.screens.profile

data class ProfileState(
    val shouldNavigateToLogin: Boolean = false,
    val notificationEnabled: Boolean = true,
    val selectedLanguage: String = "Türkçe",
    val showLogoutDialog: Boolean = false
)
