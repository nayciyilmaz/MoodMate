package com.example.moodmate.data

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

data class MoodItem(
    val emoji: String,
    val label: String
)