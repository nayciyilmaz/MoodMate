package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.moodmate.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    suspend fun isUserLoggedIn(): Boolean {
        val token = tokenManager.token.first()
        return token != null
    }
}