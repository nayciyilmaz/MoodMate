package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.UserData
import com.example.moodmate.data.UserUiState
import com.example.moodmate.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        observeTokenChanges()
    }

    private fun observeTokenChanges() {
        viewModelScope.launch {
            tokenManager.userId.collect { userId ->
                if (userId != null && userId != 0L) {
                    loadUserData()
                }
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = UserUiState(isLoading = true)
            try {
                tokenManager.userId.collect { id ->
                    tokenManager.firstName.collect { firstName ->
                        tokenManager.lastName.collect { lastName ->
                            tokenManager.userEmail.collect { email ->
                                val userData = UserData(
                                    id = id ?: 0L,
                                    firstName = firstName ?: "",
                                    lastName = lastName ?: "",
                                    email = email ?: ""
                                )

                                _uiState.value = UserUiState(
                                    isLoading = false,
                                    userData = userData
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }
}