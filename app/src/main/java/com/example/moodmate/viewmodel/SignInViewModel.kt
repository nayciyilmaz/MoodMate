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
        val validationErrors = validateInputs()
        if (validationErrors.hasErrors()) {
            _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            _actionState.value = SignInActionState(isLoading = true)
            _uiState.value = _uiState.value.copy(validationErrors = SignInValidationErrors())

            val result = authRepository.login(_uiState.value.email.trim(), _uiState.value.password)

            when (result) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        tokenManager.saveUser(response.userId, _uiState.value.email)
                        _actionState.value = SignInActionState(isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    val validationErrors = mapApiErrorToValidation(result.message)
                    _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                    _actionState.value = SignInActionState(isLoading = false)
                }
                is Resource.Loading -> {
                    _actionState.value = SignInActionState(isLoading = true)
                }
            }
        }
    }

    private fun mapApiErrorToValidation(errorMessage: String?): SignInValidationErrors {
        return when {
            errorMessage?.contains("kullanıcı bulunamadı", ignoreCase = true) == true ||
                    errorMessage?.contains("user not found", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_user_not_found)
                )
            }

            errorMessage?.contains("şifre hatalı", ignoreCase = true) == true ||
                    errorMessage?.contains("wrong password", ignoreCase = true) == true ||
                    errorMessage?.contains("password", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    passwordError = context.getString(R.string.error_wrong_password)
                )
            }

            errorMessage?.contains("email", ignoreCase = true) == true ||
                    errorMessage?.contains("şifre", ignoreCase = true) == true ||
                    errorMessage?.contains("401", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_invalid_credential),
                    passwordError = context.getString(R.string.error_invalid_credential)
                )
            }

            errorMessage?.contains("network", ignoreCase = true) == true ||
                    errorMessage?.contains("connection", ignoreCase = true) == true ||
                    errorMessage?.contains("bağlantı", ignoreCase = true) == true -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_network_request_failed)
                )
            }

            else -> {
                SignInValidationErrors(
                    emailError = context.getString(R.string.error_sign_in_failed)
                )
            }
        }
    }

    private fun validateInputs(): SignInValidationErrors {
        var errors = SignInValidationErrors()

        if (_uiState.value.email.trim().isEmpty()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_empty))
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email.trim()).matches()) {
            errors = errors.copy(emailError = context.getString(R.string.error_email_invalid))
        }

        if (_uiState.value.password.isEmpty()) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_empty))
        } else if (_uiState.value.password.length < 6) {
            errors = errors.copy(passwordError = context.getString(R.string.error_password_min_length))
        }

        return errors
    }
}