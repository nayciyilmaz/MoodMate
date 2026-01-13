package com.example.moodmate.network

import com.example.moodmate.data.AuthResponse
import com.example.moodmate.data.LoginRequest
import com.example.moodmate.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}