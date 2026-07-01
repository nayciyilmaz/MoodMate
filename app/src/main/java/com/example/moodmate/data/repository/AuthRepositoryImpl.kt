package com.example.moodmate.data.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.remote.api.ApiService
import com.example.moodmate.domain.model.AuthResponse
import com.example.moodmate.domain.model.ChangePasswordRequest
import com.example.moodmate.domain.model.ErrorResponse
import com.example.moodmate.domain.model.LoginRequest
import com.example.moodmate.domain.model.RegisterRequest
import com.example.moodmate.domain.repository.AuthRepository
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val gson = Gson()

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            handleResponse(apiService.register(RegisterRequest(firstName, lastName, email, password)))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Resource<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            handleResponse(apiService.login(LoginRequest(email, password)))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(
                ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
            )
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMessage = try {
                    gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java).message
                } catch (e: Exception) {
                    null
                }
                Resource.Error(errorMessage ?: context.getString(R.string.error_unknown))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let { Resource.Success(it) }
                ?: Resource.Error(context.getString(R.string.error_empty_response))
        } else {
            val errorBody = response.errorBody()?.string()
            val (errorMessage, fieldErrors) = try {
                if (response.code() == 400) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    Pair(null, gson.fromJson<Map<String, String>>(errorBody, type))
                } else {
                    Pair(gson.fromJson(errorBody, ErrorResponse::class.java).message, null)
                }
            } catch (e: Exception) {
                Pair(context.getString(R.string.error_server), null)
            }
            Resource.Error(errorMessage ?: context.getString(R.string.error_unknown), fieldErrors = fieldErrors)
        }
    }
}
