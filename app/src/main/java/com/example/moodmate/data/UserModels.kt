package com.example.moodmate.data

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