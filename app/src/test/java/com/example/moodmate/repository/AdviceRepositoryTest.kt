package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.dao.AdviceDao
import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.entity.AdviceLocalEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.network.ApiService
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AdviceRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var adviceDao: AdviceDao
    private lateinit var tokenManager: TokenManager
    private lateinit var context: Context
    private lateinit var repository: AdviceRepository

    @Before
    fun setUp() {
        apiService = mockk()
        adviceDao = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_unknown) } returns "Unknown error"
        every { context.getString(R.string.error_empty_response) } returns "Empty response"
        every { context.getString(R.string.error_session_expired) } returns "Session expired"

        coEvery { tokenManager.userId } returns flowOf(1L)

        repository = AdviceRepository(apiService, adviceDao, tokenManager, context)
    }

    @Test
    fun generateAdvice_whenApiCallSuccessful_shouldInsertAndReturnAdvice() = runTest {
        val adviceResponse = AdviceResponse(
            id = 1L,
            advice = "Keep up the great work!",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.generateAdvice() } returns Response.success(adviceResponse)

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Success)
        assertEquals(adviceResponse, (result as Resource.Success).data)
        coVerify {
            adviceDao.insertAdvice(
                AdviceLocalEntity(
                    userId = 1L,
                    serverId = adviceResponse.id,
                    advice = adviceResponse.advice,
                    createdAt = adviceResponse.createdAt
                )
            )
        }
    }

    @Test
    fun generateAdvice_whenApiCallFails_andCacheExists_shouldReturnCache() = runTest {
        val cachedAdvice = AdviceLocalEntity(
            userId = 1L,
            serverId = 1L,
            advice = "Cached advice",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.generateAdvice() } throws Exception("Network error")
        coEvery { adviceDao.getAdvice(1L) } returns cachedAdvice

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Success)
        assertEquals("Cached advice", (result as Resource.Success).data?.advice)
    }

    @Test
    fun generateAdvice_whenApiCallFails_andNoCacheExists_shouldReturnError() = runTest {
        coEvery { apiService.generateAdvice() } throws Exception("Network error")
        coEvery { adviceDao.getAdvice(1L) } returns null

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun generateAdvice_whenUnauthorized_shouldClearUserAndReturnError() = runTest {
        coEvery { apiService.generateAdvice() } returns Response.error(401, "".toResponseBody())
        coEvery { adviceDao.getAdvice(1L) } returns null

        val result = repository.generateAdvice()

        assertTrue(result is Resource.Error)
        assertEquals(true, (result as Resource.Error).isUnauthorized)
        coVerify { tokenManager.clearUser() }
    }

    @Test
    fun getLatestAdvice_whenApiCallSuccessful_shouldInsertAndReturnAdvice() = runTest {
        val adviceResponse = AdviceResponse(
            id = 1L,
            advice = "Try meditation",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.getLatestAdvice() } returns Response.success(adviceResponse)

        val result = repository.getLatestAdvice()

        assertTrue(result is Resource.Success)
        assertEquals(adviceResponse, (result as Resource.Success).data)
        coVerify {
            adviceDao.insertAdvice(
                AdviceLocalEntity(
                    userId = 1L,
                    serverId = adviceResponse.id,
                    advice = adviceResponse.advice,
                    createdAt = adviceResponse.createdAt
                )
            )
        }
    }

    @Test
    fun getLatestAdvice_whenApiCallFails_andCacheExists_shouldReturnCache() = runTest {
        val cachedAdvice = AdviceLocalEntity(
            userId = 1L,
            serverId = 1L,
            advice = "Cached advice",
            createdAt = "2024-02-21T10:00:00"
        )
        coEvery { apiService.getLatestAdvice() } throws Exception("Network error")
        coEvery { adviceDao.getAdvice(1L) } returns cachedAdvice

        val result = repository.getLatestAdvice()

        assertTrue(result is Resource.Success)
        assertEquals("Cached advice", (result as Resource.Success).data?.advice)
    }

    @Test
    fun getLatestAdvice_whenApiCallFails_andNoCacheExists_shouldReturnError() = runTest {
        coEvery { apiService.getLatestAdvice() } throws Exception("Network error")
        coEvery { adviceDao.getAdvice(1L) } returns null

        val result = repository.getLatestAdvice()

        assertTrue(result is Resource.Error)
    }

    @Test
    fun clearAdviceForUser_shouldCallDeleteForUser() = runTest {
        repository.clearAdviceForUser()

        coVerify { adviceDao.deleteAdviceForUser(1L) }
    }
}