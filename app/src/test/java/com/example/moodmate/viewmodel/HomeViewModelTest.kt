package com.example.moodmate.viewmodel

import com.example.moodmate.data.AdviceResponse
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var moodRepository: MoodRepository
    private lateinit var adviceRepository: AdviceRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        moodRepository = mockk()
        adviceRepository = mockk()

        coEvery { adviceRepository.getLatestAdvice() } returns Resource.Success(
            AdviceResponse(1L, "Test advice", "2024-01-01T00:00:00")
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadRecentMoods_whenSuccessful_shouldReturnTop3Moods() = runTest {
        val moodList = listOf(
            MoodResponse(1L, "😊", 8, "Good", "2024-01-01T00:00:00", "2024-01-01T00:00:00"),
            MoodResponse(2L, "😔", 4, "Bad", "2024-01-02T00:00:00", "2024-01-02T00:00:00"),
            MoodResponse(3L, "😃", 9, "Great", "2024-01-03T00:00:00", "2024-01-03T00:00:00"),
            MoodResponse(4L, "😢", 3, "Sad", "2024-01-04T00:00:00", "2024-01-04T00:00:00")
        )
        coEvery { moodRepository.getUserMoods() } returns Resource.Success(moodList)

        viewModel = HomeViewModel(moodRepository, adviceRepository)
        viewModel.loadRecentMoods()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(3, viewModel.uiState.value.moods.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun loadRecentMoods_whenUnauthorized_shouldNavigateToLogin() = runTest {
        coEvery { moodRepository.getUserMoods() } returns Resource.Error(
            message = "Session expired",
            isUnauthorized = true
        )

        viewModel = HomeViewModel(moodRepository, adviceRepository)
        viewModel.loadRecentMoods()

        assertTrue(viewModel.shouldNavigateToLogin.value)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadRecentMoods_whenError_shouldShowError() = runTest {
        coEvery { moodRepository.getUserMoods() } returns Resource.Error("Network error")

        viewModel = HomeViewModel(moodRepository, adviceRepository)
        viewModel.loadRecentMoods()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun generateAdvice_whenSuccessful_shouldUpdateAdviceState() = runTest {
        val advice = AdviceResponse(1L, "New advice", "2024-01-01T00:00:00")
        coEvery { moodRepository.getUserMoods() } returns Resource.Success(emptyList())
        coEvery { adviceRepository.generateAdvice() } returns Resource.Success(advice)

        viewModel = HomeViewModel(moodRepository, adviceRepository)
        viewModel.generateAdvice()

        assertEquals("New advice", viewModel.adviceState.value.advice)
        assertFalse(viewModel.adviceState.value.isLoading)
        assertNull(viewModel.adviceState.value.error)
    }

    @Test
    fun generateAdvice_whenError_shouldShowError() = runTest {
        coEvery { moodRepository.getUserMoods() } returns Resource.Success(emptyList())
        coEvery { adviceRepository.generateAdvice() } returns Resource.Error("AI service unavailable")

        viewModel = HomeViewModel(moodRepository, adviceRepository)
        viewModel.generateAdvice()

        assertFalse(viewModel.adviceState.value.isLoading)
        assertNotNull(viewModel.adviceState.value.error)
    }
}