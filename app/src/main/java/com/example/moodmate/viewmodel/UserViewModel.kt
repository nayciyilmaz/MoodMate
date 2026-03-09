package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.UserData
import com.example.moodmate.data.UserUiState
import com.example.moodmate.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    val uiState = combine(
        tokenManager.userId,
        tokenManager.firstName,
        tokenManager.lastName,
        tokenManager.userEmail
    ) { userId, firstName, lastName, email ->
        if (userId != null && userId != 0L) {
            UserUiState(
                isLoading = false,
                userData = UserData(
                    id = userId,
                    firstName = firstName ?: "",
                    lastName = lastName ?: "",
                    email = email ?: ""
                )
            )
        } else {
            UserUiState(isLoading = false, userData = null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserUiState(isLoading = true)
    )
}