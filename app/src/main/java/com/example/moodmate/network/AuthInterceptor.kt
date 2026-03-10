package com.example.moodmate.network

import android.content.Context
import com.example.moodmate.local.TokenManager
import com.example.moodmate.util.LocaleHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
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
                    }
                }
                addHeader("Accept-Language", languageCode)
            }
            .build()

        return chain.proceed(newRequest)
    }
}