package com.example.moodmate.repository

import com.example.moodmate.data.ErrorResponse
import com.example.moodmate.data.MoodRequest
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val gson = Gson()

    suspend fun addMood(
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MoodRequest(emoji, score, note, entryDate)
                val response = apiService.addMood(request)
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    suspend fun updateMood(
        moodId: Long,
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MoodRequest(emoji, score, note, entryDate)
                val response = apiService.updateMood(moodId, request)
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    suspend fun deleteMood(moodId: Long): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteMood(moodId)
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error("Silme işlemi başarısız")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    suspend fun getUserMoods(): Resource<List<MoodResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserMoods()
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Yanıt boş")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val (errorMessage, fieldErrors) = try {
                if (response.code() == 400) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    val validationErrors: Map<String, String> = gson.fromJson(errorBody, type)
                    Pair(null, validationErrors)
                } else {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    Pair(errorResponse.message, null)
                }
            } catch (e: Exception) {
                Pair("Sunucu hatası", null)
            }
            Resource.Error(errorMessage ?: "Bilinmeyen hata", fieldErrors = fieldErrors)
        }
    }
}