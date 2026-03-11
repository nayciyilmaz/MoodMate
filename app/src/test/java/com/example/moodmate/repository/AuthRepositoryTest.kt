package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AuthResponse
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var context: Context
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        apiService = mockk()
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_unknown) } returns "Unknown error"
        every { context.getString(R.string.error_empty_response) } returns "Empty response"
        every { context.getString(R.string.error_server) } returns "Server error"

        repository = AuthRepository(apiService, context)
    }

    @Test
    fun register_whenApiCallSuccessful_shouldReturnAuthResponse() = runTest {
        val authResponse = AuthResponse(
            id = 1L,
            firstName = "Yılmaz",
            lastName = "Naycı",
            email = "test@example.com",
            token = "test-token-123",
            createdAt = "2024-02-21T10:00:00",
            updatedAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.register(any()) } returns Response.success(authResponse)

        val result = repository.register("Yılmaz", "Naycı", "test@example.com", "password123")

        assertTrue(result is Resource.Success)
        assertEquals("test-token-123", (result as Resource.Success).data?.token)
    }

    @Test
    fun register_whenApiCallFails_shouldReturnError() = runTest {
        coEvery { apiService.register(any()) } throws Exception("Network error")

        val result = repository.register("Yılmaz", "Naycı", "test@example.com", "password123")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun register_whenValidationError_shouldReturnFieldErrors() = runTest {
        val errorJson = """{"email":"Email already exists","password":"Password too short"}"""
        coEvery { apiService.register(any()) } returns Response.error(
            400,
            errorJson.toResponseBody()
        )

        val result = repository.register("Yılmaz", "Naycı", "test@example.com", "123")

        assertTrue(result is Resource.Error)
        assertNotNull((result as Resource.Error).fieldErrors)
        assertEquals("Email already exists", result.fieldErrors?.get("email"))
    }

    @Test
    fun login_whenApiCallSuccessful_shouldReturnAuthResponse() = runTest {
        val authResponse = AuthResponse(
            id = 1L,
            firstName = "Yılmaz",
            lastName = "Naycı",
            email = "test@example.com",
            token = "test-token-123",
            createdAt = "2024-02-21T10:00:00",
            updatedAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.login(any()) } returns Response.success(authResponse)

        val result = repository.login("test@example.com", "password123")

        assertTrue(result is Resource.Success)
        assertEquals("test-token-123", (result as Resource.Success).data?.token)
    }

    @Test
    fun login_whenApiCallFails_shouldReturnError() = runTest {
        coEvery { apiService.login(any()) } throws Exception("Invalid credentials")

        val result = repository.login("test@example.com", "wrongpassword")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun login_whenServerError_shouldReturnErrorMessage() = runTest {
        val errorJson = """{"code":500,"message":"Internal server error","timestamp":"2024-02-21"}"""
        coEvery { apiService.login(any()) } returns Response.error(
            500,
            errorJson.toResponseBody()
        )

        val result = repository.login("test@example.com", "password123")

        assertTrue(result is Resource.Error)
        assertEquals("Internal server error", (result as Resource.Error).message)
    }

    @Test
    fun changePassword_whenSuccessful_shouldReturnSuccess() = runTest {
        coEvery { apiService.changePassword(any()) } returns Response.success(null)

        val result = repository.changePassword("oldPass", "newPass", "newPass")

        assertTrue(result is Resource.Success)
    }

    @Test
    fun changePassword_whenFailed_shouldReturnError() = runTest {
        val errorJson = """{"code":400,"message":"Current password is wrong","timestamp":"2024-02-21"}"""
        coEvery { apiService.changePassword(any()) } returns Response.error(
            400,
            errorJson.toResponseBody()
        )

        val result = repository.changePassword("wrongPass", "newPass", "newPass")

        assertTrue(result is Resource.Error)
        assertEquals("Current password is wrong", (result as Resource.Error).message)
    }

    @Test
    fun changePassword_whenNetworkError_shouldReturnError() = runTest {
        coEvery { apiService.changePassword(any()) } throws Exception("Network error")

        val result = repository.changePassword("oldPass", "newPass", "newPass")

        assertTrue(result is Resource.Error)
    }
}