package com.example.moodmate.presentation.screens.mood_history

import com.example.moodmate.domain.model.MoodResponse

data class MoodHistoryState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null
)
