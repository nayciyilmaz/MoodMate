package com.example.moodmate.presentation.screens.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moodmate.domain.model.MoodResponse

data class HomeState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null,
    val showSessionExpiredDialog: Boolean = false
)

data class AdviceState(
    val advice: String? = null,
    val createdAt: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SyncUiModel(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val showRetry: Boolean
)
