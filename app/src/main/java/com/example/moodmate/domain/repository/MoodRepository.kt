package com.example.moodmate.domain.repository

import com.example.moodmate.domain.model.MoodResponse
import com.example.moodmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun observeMoods(): Flow<List<MoodResponse>>
    suspend fun addMood(emoji: String, score: Int, note: String, entryDate: String): Resource<MoodResponse>
    suspend fun updateMood(moodId: Long, emoji: String, score: Int, note: String, entryDate: String): Resource<MoodResponse>
    suspend fun deleteMood(moodId: Long): Resource<Unit>
    suspend fun getUserMoods(): Resource<List<MoodResponse>>
    suspend fun clearAllMoodsForUser()
}
