package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.data.ErrorResponse
import com.example.moodmate.local.TokenManager
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdviceRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend fun generateAdvice(): Resource<AdviceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateAdvice()
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun getLatestAdvice(): Resource<AdviceResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLatestAdvice()
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    private suspend fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(context.getString(R.string.error_empty_response))
            }
        } else if (response.code() == 401) {
            tokenManager.clearUser()
            Resource.Error(
                context.getString(R.string.error_session_expired),
                isUnauthorized = true
            )
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message
            } catch (e: Exception) {
                null
            }
            Resource.Error(errorMessage ?: context.getString(R.string.error_unknown))
        }
    }
}