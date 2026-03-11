package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AuthResponse
import com.example.moodmate.data.ChangePasswordRequest
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
import timber.log.Timber
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
                Timber.d("Kayıt isteği gönderiliyor: $email")
                val request = RegisterRequest(firstName, lastName, email, password)
                val response = apiService.register(request)
                val result = handleResponse(response)
                if (result is Resource.Success) {
                    Timber.d("Kayıt başarılı: $email")
                } else if (result is Resource.Error) {
                    Timber.e("Kayıt başarısız: $email - ${result.message}")
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "Kayıt sırasında hata: $email")
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
                Timber.d("Giriş isteği gönderiliyor: $email")
                val request = LoginRequest(email, password)
                val response = apiService.login(request)
                val result = handleResponse(response)
                if (result is Resource.Success) {
                    Timber.d("Giriş başarılı: $email")
                } else if (result is Resource.Error) {
                    Timber.e("Giriş başarısız: $email - ${result.message}")
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "Giriş sırasında hata: $email")
                Resource.Error(e.localizedMessage ?: context.getString(R.string.error_unknown))
            }
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Şifre değiştirme isteği gönderiliyor")
                val request = ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
                val response = apiService.changePassword(request)
                if (response.isSuccessful) {
                    Timber.d("Şifre başarıyla değiştirildi")
                    Resource.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message
                    } catch (e: Exception) {
                        null
                    }
                    Timber.e("Şifre değiştirme başarısız: $errorMessage")
                    Resource.Error(errorMessage ?: context.getString(R.string.error_unknown))
                }
            } catch (e: Exception) {
                Timber.e(e, "Şifre değiştirme sırasında hata")
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
                Timber.e("Boş yanıt alındı")
                Resource.Error(context.getString(R.string.error_empty_response))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val (errorMessage, fieldErrors) = try {
                if (response.code() == 400) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    val validationErrors: Map<String, String> = gson.fromJson(errorBody, type)
                    Timber.e("Validasyon hatası: $validationErrors")
                    Pair(null, validationErrors)
                } else {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    Timber.e("Sunucu hatası: ${response.code()} - ${errorResponse.message}")
                    Pair(errorResponse.message, null)
                }
            } catch (e: Exception) {
                Timber.e(e, "Hata yanıtı işlenirken sorun oluştu")
                Pair(context.getString(R.string.error_server), null)
            }
            Resource.Error(errorMessage ?: context.getString(R.string.error_unknown), fieldErrors = fieldErrors)
        }
    }
}