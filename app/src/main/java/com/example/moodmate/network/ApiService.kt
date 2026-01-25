package com.example.moodmate.network

import com.example.moodmate.data.AuthResponse
import com.example.moodmate.data.LoginRequest
import com.example.moodmate.data.MoodRequest
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.data.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/moods")
    suspend fun addMood(@Body request: MoodRequest): Response<MoodResponse>

    @PUT("api/moods/{id}")
    suspend fun updateMood(
        @Path("id") id: Long,
        @Body request: MoodRequest
    ): Response<MoodResponse>

    @GET("api/moods")
    suspend fun getUserMoods(): Response<List<MoodResponse>>
}