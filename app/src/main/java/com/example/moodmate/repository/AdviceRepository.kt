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
import timber.log.Timber
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
        Timber.d("Tavsiye akışı dinleniyor: userId=$userId")
        return adviceDao.observeAdvice(userId).map { entity ->
            entity?.let { AdviceResponse(it.serverId, it.advice, it.createdAt) }
        }
    }

    suspend fun generateAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        return try {
            Timber.d("Tavsiye oluşturuluyor: userId=$userId")
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
                    Timber.d("Tavsiye oluşturuldu ve kaydedildi: userId=$userId")
                    Resource.Success(advice)
                } ?: run {
                    Timber.e("Tavsiye yanıtı boş: userId=$userId")
                    Resource.Error(context.getString(R.string.error_empty_response))
                }
            } else if (response.code() == 401) {
                Timber.e("Oturum süresi doldu: userId=$userId")
                tokenManager.clearUser()
                Resource.Error(
                    context.getString(R.string.error_session_expired),
                    isUnauthorized = true
                )
            } else {
                Timber.e("Tavsiye oluşturma başarısız: ${response.code()}, cache'e bakılıyor")
                val cached = adviceDao.getAdvice(userId)
                if (cached != null) {
                    Timber.d("Cache'den tavsiye döndürüldü: userId=$userId")
                    Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
                } else {
                    Timber.e("Cache'de tavsiye bulunamadı: userId=$userId")
                    Resource.Error(context.getString(R.string.error_unknown))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Tavsiye oluşturma sırasında hata: userId=$userId, cache'e bakılıyor")
            val cached = adviceDao.getAdvice(userId)
            if (cached != null) {
                Timber.d("Cache'den tavsiye döndürüldü: userId=$userId")
                Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
            } else {
                Timber.e("Cache'de tavsiye bulunamadı: userId=$userId")
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun getLatestAdvice(): Resource<AdviceResponse> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        return try {
            Timber.d("Son tavsiye getiriliyor: userId=$userId")
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
                    Timber.d("Son tavsiye getirildi ve kaydedildi: userId=$userId")
                    Resource.Success(advice)
                } ?: run {
                    Timber.e("Son tavsiye yanıtı boş: userId=$userId")
                    Resource.Error(context.getString(R.string.error_empty_response))
                }
            } else {
                Timber.e("Son tavsiye getirme başarısız: ${response.code()}, cache'e bakılıyor")
                val cached = adviceDao.getAdvice(userId)
                if (cached != null) {
                    Timber.d("Cache'den tavsiye döndürüldü: userId=$userId")
                    Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
                } else {
                    Timber.e("Cache'de tavsiye bulunamadı: userId=$userId")
                    Resource.Error(context.getString(R.string.error_unknown))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Son tavsiye getirme sırasında hata: userId=$userId, cache'e bakılıyor")
            val cached = adviceDao.getAdvice(userId)
            if (cached != null) {
                Timber.d("Cache'den tavsiye döndürüldü: userId=$userId")
                Resource.Success(AdviceResponse(cached.serverId, cached.advice, cached.createdAt))
            } else {
                Timber.e("Cache'de tavsiye bulunamadı: userId=$userId")
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun clearAdviceForUser() {
        val userId = tokenManager.userId.first() ?: return
        Timber.d("Tavsiye siliniyor: userId=$userId")
        adviceDao.deleteAdviceForUser(userId)
        Timber.d("Tavsiye silindi: userId=$userId")
    }
}