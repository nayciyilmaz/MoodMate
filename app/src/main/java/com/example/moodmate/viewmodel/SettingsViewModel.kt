package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.SettingsActionState
import com.example.moodmate.data.SettingsUiState
import com.example.moodmate.data.SettingsValidationErrors
import com.example.moodmate.repository.AuthRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(SettingsActionState())
    val actionState: StateFlow<SettingsActionState> = _actionState.asStateFlow()

    fun onCurrentPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password,
            validationErrors = _uiState.value.validationErrors.copy(currentPasswordError = null)
        )
    }

    fun onNewPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            validationErrors = _uiState.value.validationErrors.copy(newPasswordError = null)
        )
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password,
            validationErrors = _uiState.value.validationErrors.copy(confirmPasswordError = null)
        )
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isCurrentPasswordVisible = !_uiState.value.isCurrentPasswordVisible
        )
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isNewPasswordVisible = !_uiState.value.isNewPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun changePassword() {
        val state = _uiState.value
        val errors = validateFields(state)

        if (errors != null) {
            Timber.w("Şifre değiştirme başarısız: validasyon hatası")
            _uiState.value = _uiState.value.copy(validationErrors = errors)
            return
        }

        viewModelScope.launch {
            _actionState.value = SettingsActionState(isLoading = true)
            _uiState.value = _uiState.value.copy(validationErrors = SettingsValidationErrors())

            Timber.d("Şifre değiştirme isteği gönderiliyor")

            val result = authRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword
            )

            when (result) {
                is Resource.Success -> {
                    Timber.d("Şifre başarıyla değiştirildi")
                    _actionState.value = SettingsActionState(isSuccess = true)
                }
                is Resource.Error -> {
                    Timber.e("Şifre değiştirme başarısız: ${result.message}")
                    val validationErrors = mapErrorToValidation(result.message, result.fieldErrors)
                    _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                    _actionState.value = SettingsActionState(isLoading = false)
                }
                is Resource.Loading -> {
                    _actionState.value = SettingsActionState(isLoading = true)
                }
            }
        }
    }

    private fun validateFields(state: SettingsUiState): SettingsValidationErrors? {
        val currentPasswordError = if (state.currentPassword.isBlank()) {
            context.getString(R.string.error_current_password_empty)
        } else null

        val newPasswordError = if (state.newPassword.isBlank()) {
            context.getString(R.string.error_new_password_empty)
        } else if (state.newPassword.length < 6) {
            context.getString(R.string.error_password_min_length)
        } else if (state.newPassword == state.currentPassword) {
            context.getString(R.string.error_password_same_as_current)
        } else null

        val confirmPasswordError = if (state.confirmPassword.isBlank()) {
            context.getString(R.string.error_confirm_password_empty)
        } else if (state.newPassword != state.confirmPassword) {
            context.getString(R.string.error_passwords_do_not_match)
        } else null

        return if (currentPasswordError != null || newPasswordError != null || confirmPasswordError != null) {
            SettingsValidationErrors(
                currentPasswordError = currentPasswordError,
                newPasswordError = newPasswordError,
                confirmPasswordError = confirmPasswordError
            )
        } else null
    }

    private fun mapErrorToValidation(
        message: String?,
        fieldErrors: Map<String, String>?
    ): SettingsValidationErrors {
        return if (fieldErrors != null) {
            SettingsValidationErrors(
                currentPasswordError = fieldErrors["currentPassword"],
                newPasswordError = fieldErrors["newPassword"],
                confirmPasswordError = fieldErrors["confirmPassword"]
            )
        } else {
            SettingsValidationErrors(
                currentPasswordError = message ?: context.getString(R.string.error_change_password_failed)
            )
        }
    }
}