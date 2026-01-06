package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.SignUpActionState
import com.example.moodmate.data.SignUpUiState
import com.example.moodmate.data.SignUpValidationErrors
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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(SignUpActionState())
    val actionState: StateFlow<SignUpActionState> = _actionState.asStateFlow()

    fun onFirstNameChange(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            validationErrors = _uiState.value.validationErrors.copy(firstNameError = null)
        )
    }

    fun onLastNameChange(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            validationErrors = _uiState.value.validationErrors.copy(lastNameError = null)
        )
    }

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

    fun register() {
        val validationErrors = validateInputs()
        if (validationErrors.hasErrors()) {
            _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
            return
        }

        viewModelScope.launch {
            _actionState.value = SignUpActionState(isLoading = true)
            _uiState.value = _uiState.value.copy(validationErrors = SignUpValidationErrors())

            val result = authRepository.register(
                _uiState.value.firstName.trim(),
                _uiState.value.lastName.trim(),
                _uiState.value.email.trim(),
                _uiState.value.password
            )

            when (result) {
                is Resource.Success -> {
                    _actionState.value = SignUpActionState(isSuccess = true)
                }
                is Resource.Error -> {
                    val validationErrors = mapApiErrorToValidation(result.message)
                    _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                    _actionState.value = SignUpActionState(isLoading = false)
                }
                is Resource.Loading -> {
                    _actionState.value = SignUpActionState(isLoading = true)
                }
            }
        }
    }

    private fun mapApiErrorToValidation(errorMessage: String?): SignUpValidationErrors {
        return when {
            errorMessage?.contains("email", ignoreCase = true) == true &&
                    (errorMessage.contains("kayıtlı", ignoreCase = true) ||
                            errorMessage.contains("already", ignoreCase = true) ||
                            errorMessage.contains("409", ignoreCase = true)) -> {
                SignUpValidationErrors(
                    emailError = context.getString(R.string.error_email_already_exists)
                )
            }

            errorMessage?.contains("geçersiz", ignoreCase = true) == true ||
                    errorMessage?.contains("invalid", ignoreCase = true) == true ||
                    errorMessage?.contains("400", ignoreCase = true) == true -> {
                SignUpValidationErrors(
                    emailError = context.getString(R.string.error_email_invalid)
                )
            }

            errorMessage?.contains("network", ignoreCase = true) == true ||
                    errorMessage?.contains("connection", ignoreCase = true) == true ||
                    errorMessage?.contains("bağlantı", ignoreCase = true) == true -> {
                SignUpValidationErrors(
                    emailError = context.getString(R.string.error_network_request_failed)
                )
            }

            else -> {
                SignUpValidationErrors(
                    emailError = context.getString(R.string.error_sign_up_failed)
                )
            }
        }
    }

    private fun validateInputs(): SignUpValidationErrors {
        var errors = SignUpValidationErrors()

        if (_uiState.value.firstName.trim().isEmpty()) {
            errors = errors.copy(firstNameError = context.getString(R.string.error_firstname_empty))
        }

        if (_uiState.value.lastName.trim().isEmpty()) {
            errors = errors.copy(lastNameError = context.getString(R.string.error_lastname_empty))
        }

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