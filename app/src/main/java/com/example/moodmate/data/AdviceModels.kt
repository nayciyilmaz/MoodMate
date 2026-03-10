package com.example.moodmate.data

data class AdviceResponse(
    val id: Long,
    val advice: String,
    val createdAt: String
)

data class AdviceUiState(
    val advice: String? = null,
    val createdAt: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)