package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.SignInActionState
import com.example.moodmate.data.SignInUiState
import com.example.moodmate.data.SignInValidationErrors
import com.example.moodmate.local.TokenManager
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.AuthRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val moodRepository: MoodRepository,
    private val adviceRepository: AdviceRepository,
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

            val existingUserId = tokenManager.userId.first()
            Timber.d("Giriş başlatıldı: email=${_uiState.value.email}")

            val result = authRepository.login(
                _uiState.value.email.trim(),
                _uiState.value.password
            )

            when (result) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        if (existingUserId != null && existingUserId != 0L && existingUserId != response.id) {
                            Timber.d("Farklı kullanıcı girişi, önceki veriler temizleniyor: existingUserId=$existingUserId, newUserId=${response.id}")
                            moodRepository.clearAllMoodsForUser()
                            adviceRepository.clearAdviceForUser()
                        }
                        tokenManager.saveUser(
                            token = response.token,
                            userId = response.id,
                            email = response.email,
                            firstName = response.firstName,
                            lastName = response.lastName
                        )
                        Timber.d("Giriş başarılı: userId=${response.id}, email=${response.email}")
                        _actionState.value = SignInActionState(isSuccess = true)
                    }
                }
                is Resource.Error -> {
                    Timber.e("Giriş başarısız: email=${_uiState.value.email}, hata=${result.message}")
                    val validationErrors = mapErrorToValidation(result.message, result.fieldErrors)
                    _uiState.value = _uiState.value.copy(validationErrors = validationErrors)
                    _actionState.value = SignInActionState(isLoading = false)
                }
                else -> {}
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