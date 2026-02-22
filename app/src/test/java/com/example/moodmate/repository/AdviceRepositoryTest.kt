package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.local.TokenManager
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AdviceRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var context: Context
    private lateinit var repository: AdviceRepository

    @Before
    fun setUp() {
        apiService = mockk()
        tokenManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_unknown) } returns "Unknown error"
        every { context.getString(R.string.error_empty_response) } returns "Empty response"
        every { context.getString(R.string.error_session_expired) } returns "Session expired"

        repository = AdviceRepository(apiService, tokenManager, context)
    }

    @Test
    fun generateAdvice_whenApiCallSuccessful_shouldReturnAdvice() = runTest {
        val adviceResponse = AdviceResponse(
            id = 1L,
            advice = "Keep up the great work!",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.generateAdvice() } returns Response.success(adviceResponse)

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Success)
        assertEquals(adviceResponse, (result as Resource.Success).data)
    }

    @Test
    fun generateAdvice_whenApiCallFails_shouldReturnError() = runTest {
        coEvery { apiService.generateAdvice() } throws Exception("Network error")

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun getLatestAdvice_whenApiCallSuccessful_shouldReturnAdvice() = runTest {
        val adviceResponse = AdviceResponse(
            id = 1L,
            advice = "Try meditation",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.getLatestAdvice() } returns Response.success(adviceResponse)

        val result = repository.getLatestAdvice()

        assertTrue(result is Resource.Success)
        assertEquals(adviceResponse, (result as Resource.Success).data)
    }

    @Test
    fun getLatestAdvice_whenUnauthorized_shouldClearTokenAndReturnError() = runTest {
        coEvery {
            apiService.getLatestAdvice()
        } returns Response.error(401, "".toResponseBody())

        val result = repository.getLatestAdvice()

        assertTrue(result is Resource.Error)
        assertEquals(true, (result as Resource.Error).isUnauthorized)
    }
}