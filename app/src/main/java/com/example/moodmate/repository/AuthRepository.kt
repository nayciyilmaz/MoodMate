package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AuthResponse
import com.example.moodmate.data.ErrorResponse
import com.example.moodmate.data.LoginRequest
import com.example.moodmate.data.RegisterRequest
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(firstName, lastName, email, password)
                val response = apiService.register(request)
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(context.getString(R.string.error_empty_response))
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
                Pair(context.getString(R.string.error_server), null)
            }
            Resource.Error(errorMessage ?: context.getString(R.string.error_unknown), fieldErrors = fieldErrors)
        }
    }
}