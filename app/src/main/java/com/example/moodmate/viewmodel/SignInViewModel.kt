package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.SignInActionState
import com.example.moodmate.data.SignInUiState
import com.example.moodmate.data.SignInValidationErrors
import com.example.moodmate.local.TokenManager
import com.example.moodmate.repository.AuthRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(SignInActionState())
    val actionState: StateFlow<SignInActionState> = _actionState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            validationErrors = _uiState.value.validationErrors.copy(emailError = null)
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            validationErrors = _uiState.value.validationErrors.copy(passwordError = null)
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun login() {
        viewModelScope.launch {
            _actionState.value = SignInActionState(isLoading = true)
            _uiState.value = _uiState.value.copy(validationErrors = SignInValidationErrors())

            val result = authRepository.login(_uiState.value.email.trim(), _uiState.value.password)

            when (result) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        tokenManager.saveUser(
                            token = response.token,
                            userId = response.id,
                            email = response.email,
                            firstName = response.firstName,
                            lastName = response.lastName
                        )
                        _actionState.value = SignInActionState(isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    val validationErrors = mapErrorToValidation(result.message, result.fieldErrors)
                    _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                    _actionState.value = SignInActionState(isLoading = false)
                }
                is Resource.Loading -> {
                    _actionState.value = SignInActionState(isLoading = true)
                }
            }
        }
    }

    private fun mapErrorToValidation(
        message: String?,
        fieldErrors: Map<String, String>?
    ): SignInValidationErrors {
        return if (fieldErrors != null) {
            SignInValidationErrors(
                emailError = fieldErrors["email"],
                passwordError = fieldErrors["password"]
            )
        } else {
            SignInValidationErrors(
                emailError = message ?: context.getString(R.string.error_sign_in_failed)
            )
        }
    }
}