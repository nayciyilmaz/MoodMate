package com.example.moodmate.domain.repository

import com.example.moodmate.domain.model.AuthResponse
import com.example.moodmate.util.Resource

interface AuthRepository {
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Resource<AuthResponse>
    suspend fun login(email: String, password: String): Resource<AuthResponse>
    suspend fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String): Resource<Unit>
}
