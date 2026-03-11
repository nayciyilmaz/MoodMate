package com.example.moodmate.viewmodel

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.util.NetworkMonitor
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var moodRepository: MoodRepository
    private lateinit var adviceRepository: AdviceRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var syncManager: SyncManager
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var moodDao: MoodDao
    private lateinit var context: Context

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMoodEntity = MoodEntity(
        localId = "local-123",
        serverId = 1L,
        userId = 1L,
        emoji = "😊",
        score = 8,
        note = "Great day",
        entryDate = "2024-01-01T00:00:00",
        createdAt = "2024-01-01T00:00:00",
        updatedAt = "2024-01-01T00:00:00",
        syncStatus = SyncStatus.SYNCED
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        moodRepository = mockk(relaxed = true)
        adviceRepository = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        networkMonitor = mockk(relaxed = true)
        moodDao = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_ai_unavailable) } returns "AI unavailable"
        coEvery { tokenManager.isTokenValid() } returns true
        coEvery { networkMonitor.isOnline } returns flowOf(true)
        coEvery { moodDao.getPendingMoods() } returns emptyList()
        coEvery { adviceRepository.observeAdvice() } returns flowOf(null)
        coEvery { moodRepository.observeMoods() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(
            moodRepository,
            adviceRepository,
            tokenManager,
            syncManager,
            syncScheduler,
            networkMonitor,
            moodDao,
            context
        )
    }

    @Test
    fun init_whenMoodsExist_shouldShowTop3Moods() = runTest {
        val moods = listOf(
            MoodResponse(1L, "😊", 8, "Good", "2024-01-01T00:00:00", "2024-01-01T00:00:00"),
            MoodResponse(2L, "😔", 4, "Bad", "2024-01-02T00:00:00", "2024-01-02T00:00:00"),
            MoodResponse(3L, "😃", 9, "Great", "2024-01-03T00:00:00", "2024-01-03T00:00:00"),
            MoodResponse(4L, "😢", 3, "Sad", "2024-01-04T00:00:00", "2024-01-04T00:00:00")
        )
        coEvery { moodRepository.observeMoods() } returns flowOf(moods)

        createViewModel()

        assertEquals(3, viewModel.uiState.value.moods.size)
    }

    @Test
    fun init_whenTokenInvalid_shouldShowSessionExpiredDialog() = runTest {
        coEvery { tokenManager.isTokenValid() } returns false

        createViewModel()

        assertTrue(viewModel.uiState.value.showSessionExpiredDialog)
    }

    @Test
    fun init_whenOffline_shouldUpdatePendingState() = runTest {
        coEvery { networkMonitor.isOnline } returns flowOf(false)
        coEvery { moodDao.getPendingMoods() } returns listOf(testMoodEntity)

        createViewModel()

        coEvery { syncManager.updatePendingState(1) }
    }

    @Test
    fun init_whenAdviceExists_shouldUpdateAdviceState() = runTest {
        val advice = AdviceResponse(1L, "Test advice", "2024-01-01T00:00:00")
        coEvery { adviceRepository.observeAdvice() } returns flowOf(advice)

        createViewModel()

        assertEquals("Test advice", viewModel.adviceState.value.advice)
    }

    @Test
    fun loadRecentMoods_whenUnauthorized_shouldShowSessionExpiredDialog() = runTest {
        coEvery { moodRepository.getUserMoods() } returns Resource.Error(
            message = "Session expired",
            isUnauthorized = true
        )

        createViewModel()
        viewModel.loadRecentMoods()

        assertTrue(viewModel.uiState.value.showSessionExpiredDialog)
    }

    @Test
    fun generateAdvice_whenSuccessful_shouldUpdateAdviceState() = runTest {
        val advice = AdviceResponse(1L, "New advice", "2024-01-01T00:00:00")
        coEvery { adviceRepository.generateAdvice() } returns Resource.Success(advice)

        createViewModel()
        viewModel.generateAdvice()

        assertEquals("New advice", viewModel.adviceState.value.advice)
        assertFalse(viewModel.adviceState.value.isLoading)
        assertNull(viewModel.adviceState.value.error)
    }

    @Test
    fun generateAdvice_whenError_shouldShowError() = runTest {
        coEvery { adviceRepository.generateAdvice() } returns Resource.Error("AI service unavailable")

        createViewModel()
        viewModel.generateAdvice()

        assertFalse(viewModel.adviceState.value.isLoading)
        assertNotNull(viewModel.adviceState.value.error)
    }

    @Test
    fun generateAdvice_whenUnauthorized_shouldShowSessionExpiredDialog() = runTest {
        coEvery { adviceRepository.generateAdvice() } returns Resource.Error(
            message = "Session expired",
            isUnauthorized = true
        )

        createViewModel()
        viewModel.generateAdvice()

        assertTrue(viewModel.uiState.value.showSessionExpiredDialog)
    }

    @Test
    fun navigateToLoginAfterSessionExpiry_shouldSetNavigationFlag() = runTest {
        createViewModel()
        viewModel.navigateToLoginAfterSessionExpiry()

        assertTrue(viewModel.shouldNavigateToLogin.value)
    }

    @Test
    fun resetNavigationFlag_shouldClearFlag() = runTest {
        createViewModel()
        viewModel.navigateToLoginAfterSessionExpiry()
        viewModel.resetNavigationFlag()

        assertFalse(viewModel.shouldNavigateToLogin.value)
    }
}