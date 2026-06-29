package com.example.moodmate.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)