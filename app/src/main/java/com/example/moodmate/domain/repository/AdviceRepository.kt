package com.example.moodmate.domain.repository

import com.example.moodmate.domain.model.AdviceResponse
import com.example.moodmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface AdviceRepository {
    suspend fun observeAdvice(): Flow<AdviceResponse?>
    suspend fun generateAdvice(): Resource<AdviceResponse>
    suspend fun getLatestAdvice(): Resource<AdviceResponse>
    suspend fun clearAdviceForUser()
}
