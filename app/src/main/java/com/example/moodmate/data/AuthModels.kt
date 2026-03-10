package com.example.moodmate.data

import com.google.gson.annotations.SerializedName

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

data class ErrorResponse(
    val code: Int?,
    val message: String?,
    val timestamp: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)