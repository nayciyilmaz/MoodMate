package com.example.moodmate.repository

import com.example.moodmate.data.LoginRequest
import com.example.moodmate.data.LoginResponse
import com.example.moodmate.data.RegisterRequest
import com.example.moodmate.data.RegisterResponse
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Resource<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(firstName, lastName, email, password)
                val response = apiService.register(request)
                handleResponse(response)
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Resource<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)
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
            val errorMsg = when (response.code()) {
                400 -> "Geçersiz istek"
                401 -> "Kullanıcı bulunamadı veya şifre hatalı"
                409 -> "Bu email zaten kayıtlı"
                500 -> "Sunucu hatası"
                else -> "Bilinmeyen hata: ${response.code()}"
            }
            Resource.Error(errorMsg)
        }
    }
}