package com.example.moodmate.data

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
)

data class UserData(
    val id: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName"
}

data class UserUiState(
    val isLoading: Boolean = true,
    val userData: UserData? = null,
    val error: String? = null
)

data class RegisterRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val id: Long,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val token: String,
    val createdAt: String,
    val updatedAt: String
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

data class ErrorResponse(
    val code: Int?,
    val message: String?,
    val timestamp: String?
)

data class MoodRequest(
    val emoji: String,
    val score: Int,
    val note: String,
    val entryDate: String
)

data class MoodResponse(
    val id: Long,
    val emoji: String,
    val score: Int,
    val note: String,
    val entryDate: String,
    val createdAt: String
)

data class MoodItem(
    val emoji: String,
    val label: String
)

data class AddMoodUiState(
    val selectedMoodIndex: Int = -1,
    val selectedRating: Int = -1,
    val noteText: String = "",
    val validationError: String? = null
) {
    fun isValid(): Boolean {
        return selectedMoodIndex >= 0 &&
                selectedRating > 0 &&
                noteText.isNotBlank()
    }
}

data class AddMoodActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null
)

data class MoodHistoryUiState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null
)

data class AdviceResponse(
    val id: Long,
    val advice: String,
    val createdAt: String
)

data class AdviceUiState(
    val advice: String? = null,
    val createdAt: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)