package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.dao.AdviceDao
import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.entity.AdviceLocalEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdviceRepository @Inject constructor(
    private val apiService: ApiService,
    private val adviceDao: AdviceDao,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    suspend fun observeAdvice(): Flow<AdviceResponse?> {
        val userId = tokenManager.userId.first() ?: 0L
        return adviceDao.observeAdvice(userId).map { entity ->
            entity?.let { AdviceResponse(it.serverId, it.advice, it.createdAt) }
        }
    }

    suspend fun generateAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        return try {
            val response = apiService.generateAdvice()
            if (response.isSuccessful) {
                response.body()?.let { advice ->
                    adviceDao.insertAdvice(
                        AdviceLocalEntity(
                            userId = userId,
                            serverId = advice.id,
                            advice = advice.advice,
                            createdAt = advice.createdAt
                        )
                    )
                    Resource.Success(advice)
                } ?: Resource.Error(context.getString(R.string.error_empty_response))
            } else if (response.code() == 401) {
                tokenManager.clearUser()
                Resource.Error(
                    context.getString(R.string.error_session_expired),
                    isUnauthorized = true
                )
            } else {
                val cached = adviceDao.getAdvice(userId)
                if (cached != null) {
                    Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
                } else {
                    Resource.Error(context.getString(R.string.error_unknown))
                }
            }
        } catch (e: Exception) {
            val cached = adviceDao.getAdvice(userId)
            if (cached != null) {
                Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
            } else {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun getLatestAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        return try {
            val response = apiService.getLatestAdvice()
            if (response.isSuccessful) {
                response.body()?.let { advice ->
                    adviceDao.insertAdvice(
                        AdviceLocalEntity(
                            userId = userId,
                            serverId = advice.id,
                            advice = advice.advice,
                            createdAt = advice.createdAt
                        )
                    )
                    Resource.Success(advice)
                } ?: Resource.Error(context.getString(R.string.error_empty_response))
            } else {
                val cached = adviceDao.getAdvice(userId)
                if (cached != null) {
                    Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
                } else {
                    Resource.Error(context.getString(R.string.error_unknown))
                }
            }
        } catch (e: Exception) {
            val cached = adviceDao.getAdvice(userId)
            if (cached != null) {
                Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
            } else {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun clearAdviceForUser() {
        val userId = tokenManager.userId.first() ?: return
        adviceDao.deleteAdviceForUser(userId)
    }
}