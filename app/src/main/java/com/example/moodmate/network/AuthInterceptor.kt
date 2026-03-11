package com.example.moodmate.network

import android.content.Context
import com.example.moodmate.local.TokenManager
import com.example.moodmate.util.LocaleHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val token = runBlocking {
            tokenManager.token.first()
        }

        val languageCode = LocaleHelper.getLanguage(context)

        val newRequest = request.newBuilder()
            .apply {
                if (!request.url.encodedPath.contains("/api/auth/")) {
                    token?.let {
                        addHeader("Authorization", "Bearer $it")
                        Timber.d("Token eklendi: ${request.url.encodedPath}")
                    } ?: Timber.w("Token bulunamadı: ${request.url.encodedPath}")
                }
                addHeader("Accept-Language", languageCode)
                Timber.d("İstek gönderiliyor: ${request.method} ${request.url.encodedPath} - Dil: $languageCode")
            }
            .build()

        val response = chain.proceed(newRequest)
        Timber.d("Yanıt alındı: ${response.code} - ${request.url.encodedPath}")

        if (!response.isSuccessful) {
            Timber.e("Hata yanıtı: ${response.code} - ${request.url.encodedPath}")
        }

        return response
    }
}