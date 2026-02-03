package com.example.moodmate.repository

import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.data.ErrorResponse
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdviceRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val gson = Gson()

    suspend fun generateAdvice(): Resource<AdviceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateAdvice()
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    suspend fun getLatestAdvice(): Resource<AdviceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLatestAdvice()
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
            val errorMessage = try {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message
            } catch (e: Exception) {
                null
            }
            Resource.Error(errorMessage ?: "Bilinmeyen hata")
        }
    }
}