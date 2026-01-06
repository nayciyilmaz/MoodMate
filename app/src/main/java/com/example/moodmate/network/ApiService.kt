package com.example.moodmate.network

import com.example.moodmate.data.LoginRequest
import com.example.moodmate.data.LoginResponse
import com.example.moodmate.data.RegisterRequest
import com.example.moodmate.data.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}