package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.MoodResponse
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

class MoodRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var context: Context
    private lateinit var repository: MoodRepository

    @Before
    fun setUp() {
        apiService = mockk()
        tokenManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_unknown) } returns "Unknown error"
        every { context.getString(R.string.error_empty_response) } returns "Empty response"
        every { context.getString(R.string.error_session_expired) } returns "Session expired"
        every { context.getString(R.string.error_delete_failed) } returns "Delete failed"
        every { context.getString(R.string.error_server) } returns "Server error"

        repository = MoodRepository(apiService, tokenManager, context)
    }

    @Test
    fun addMood_whenApiCallSuccessful_shouldReturnSuccess() = runTest {
        val moodResponse = MoodResponse(
            id = 1L,
            emoji = "😊",
            score = 8,
            note = "Great day",
            entryDate = "2024-02-21T10:00:00",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery {
            apiService.addMood(any())
        } returns Response.success(moodResponse)

        val result = repository.addMood("😊", 8, "Great day", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Success)
        assertEquals(moodResponse, (result as Resource.Success).data)
    }

    @Test
    fun addMood_whenApiCallFails_shouldReturnError() = runTest {
        coEvery {
            apiService.addMood(any())
        } throws Exception("Network error")

        val result = repository.addMood("😊", 8, "Great day", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun getUserMoods_whenApiCallSuccessful_shouldReturnMoodList() = runTest {
        val moodList = listOf(
            MoodResponse(1L, "😊", 8, "Good", "2024-02-21T10:00:00", "2024-02-21T10:00:00"),
            MoodResponse(2L, "😔", 4, "Bad", "2024-02-20T10:00:00", "2024-02-20T10:00:00")
        )
        coEvery { apiService.getUserMoods() } returns Response.success(moodList)

        val result = repository.getUserMoods()

        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success
        assertEquals(2, successResult.data?.size)
    }

    @Test
    fun updateMood_whenApiCallSuccessful_shouldReturnUpdatedMood() = runTest {
        val updatedMood = MoodResponse(
            id = 1L,
            emoji = "😊",
            score = 9,
            note = "Updated",
            entryDate = "2024-02-21T10:00:00",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery {
            apiService.updateMood(any(), any())
        } returns Response.success(updatedMood)

        val result = repository.updateMood(1L, "😊", 9, "Updated", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success
        assertEquals(9, successResult.data?.score)
    }

    @Test
    fun deleteMood_whenApiCallSuccessful_shouldReturnSuccess() = runTest {
        coEvery { apiService.deleteMood(any()) } returns Response.success(null)

        val result = repository.deleteMood(1L)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun deleteMood_whenUnauthorized_shouldClearTokenAndReturnError() = runTest {
        coEvery {
            apiService.deleteMood(any())
        } returns Response.error(401, "".toResponseBody())

        val result = repository.deleteMood(1L)

        assertTrue(result is Resource.Error)
        val errorResult = result as Resource.Error
        assertEquals(true, errorResult.isUnauthorized)
    }
}