package com.example.moodmate.domain.model

data class MoodRequest(
    val emoji: String,
    val score: Int,
    val note: String,
    val entryDate: String
)

data class MoodResponse(
    val id: Long,
    val emoji: String,
    val score: Int,
    val note: String,
    val entryDate: String,
    val createdAt: String
)
