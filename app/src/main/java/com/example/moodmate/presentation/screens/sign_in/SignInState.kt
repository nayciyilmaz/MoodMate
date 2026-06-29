package com.example.moodmate.presentation.screens.sign_in

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val validationErrors: SignInValidationErrors = SignInValidationErrors()
)

data class SignInActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class SignInValidationErrors(
    val emailError: String? = null,
    val passwordError: String? = null
)
