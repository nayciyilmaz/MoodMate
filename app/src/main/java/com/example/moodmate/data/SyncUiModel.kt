package com.example.moodmate.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SyncUiModel(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val showRetry: Boolean
)