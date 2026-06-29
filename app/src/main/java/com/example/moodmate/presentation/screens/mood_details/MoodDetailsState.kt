package com.example.moodmate.presentation.screens.mood_details

import com.example.moodmate.domain.model.MoodResponse

data class MoodDetailsState(
    val moodDetails: MoodResponse? = null,
    val showDeleteDialog: Boolean = false,
    val deleteSuccess: Boolean = false
)
