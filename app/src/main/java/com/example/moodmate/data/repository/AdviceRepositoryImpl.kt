package com.example.moodmate.data.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.local.datastore.TokenManager
import com.example.moodmate.data.local.room.AdviceDao
import com.example.moodmate.data.local.room.AdviceLocalEntity
import com.example.moodmate.data.remote.api.ApiService
import com.example.moodmate.domain.model.AdviceResponse
import com.example.moodmate.domain.repository.AdviceRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdviceRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val adviceDao: AdviceDao,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : AdviceRepository {

    override suspend fun observeAdvice(): Flow<AdviceResponse?> {
        val userId = tokenManager.userId.first() ?: 0L
        return adviceDao.observeAdvice(userId).map { entity ->
            entity?.let { AdviceResponse(it.serverId, it.advice, it.createdAt) }
        }
    }

    override suspend fun generateAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first()
            ?: return Resource.Error(context.getString(R.string.error_unknown))
        return try {
            val response = apiService.generateAdvice()
            when {
                response.isSuccessful -> response.body()?.let { advice ->
                    adviceDao.insertAdvice(AdviceLocalEntity(userId, advice.id, advice.advice, advice.createdAt))
                    Resource.Success(advice)
                } ?: Resource.Error(context.getString(R.string.error_empty_response))
                response.code() == 401 -> {
                    tokenManager.clearUser()
                    Resource.Error(context.getString(R.string.error_session_expired), isUnauthorized = true)
                }
                else -> getCachedAdvice(userId) ?: Resource.Error(context.getString(R.string.error_unknown))
            }
        } catch (e: Exception) {
            getCachedAdvice(userId) ?: Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
        }
    }

    override suspend fun getLatestAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first()
            ?: return Resource.Error(context.getString(R.string.error_unknown))
        return try {
            val response = apiService.getLatestAdvice()
            if (response.isSuccessful) {
                response.body()?.let { advice ->
                    adviceDao.insertAdvice(AdviceLocalEntity(userId, advice.id, advice.advice, advice.createdAt))
                    Resource.Success(advice)
                } ?: Resource.Error(context.getString(R.string.error_empty_response))
            } else {
                getCachedAdvice(userId) ?: Resource.Error(context.getString(R.string.error_unknown))
            }
        } catch (e: Exception) {
            getCachedAdvice(userId) ?: Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
        }
    }

    override suspend fun clearAdviceForUser() {
        val userId = tokenManager.userId.first() ?: return
        adviceDao.deleteAdviceForUser(userId)
    }

    private suspend fun getCachedAdvice(userId: Long): Resource<AdviceResponse>? {
        return adviceDao.getAdvice(userId)?.let {
            Resource.Success(AdviceResponse(it.serverId, it.advice, it.createdAt))
        }
    }
}
