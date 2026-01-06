package com.example.moodmate.data

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

data class RegisterRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    @SerializedName("userId")
    val userId: Int
)

data class SignInUiState(
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
) {
    fun hasErrors(): Boolean {
        return emailError != null || passwordError != null
    }
}

data class SignUpUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val validationErrors: SignUpValidationErrors = SignUpValidationErrors()
)

data class SignUpActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class SignUpValidationErrors(
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
) {
    fun hasErrors(): Boolean {
        return firstNameError != null || lastNameError != null ||
                emailError != null || passwordError != null
    }
}