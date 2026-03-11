package com.example.moodmate.viewmodel

import com.example.moodmate.data.MoodResponse
import com.example.moodmate.repository.MoodRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MoodHistoryViewModelTest {

    private lateinit var viewModel: MoodHistoryViewModel
    private lateinit var moodRepository: MoodRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testMoods = listOf(
        MoodResponse(1L, "😊", 8, "Great day", "2024-01-01T10:00:00", "2024-01-01T10:00:00"),
        MoodResponse(2L, "😔", 4, "Bad day", "2024-01-02T10:00:00", "2024-01-02T10:00:00"),
        MoodResponse(3L, "😃", 9, "Amazing day", "2024-01-03T10:00:00", "2024-01-03T10:00:00")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        moodRepository = mockk(relaxed = true)
        coEvery { moodRepository.observeMoods() } returns flowOf(testMoods)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_shouldLoadAllMoods() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(3, viewModel.uiState.value.moods.size)
    }

    @Test
    fun onSearchTextChange_shouldFilterMoodsByNote() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        viewModel.onSearchTextChange("Great")

        assertEquals("Great", viewModel.searchText)
        assertEquals(1, viewModel.uiState.value.moods.size)
        assertEquals("Great day", viewModel.uiState.value.moods[0].note)
    }

    @Test
    fun onClearSearch_shouldShowAllMoods() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        viewModel.onSearchTextChange("Great")
        viewModel.onClearSearch()

        assertEquals("", viewModel.searchText)
        assertEquals(3, viewModel.uiState.value.moods.size)
    }

    @Test
    fun onDateSelect_shouldUpdateTempSelectedDate() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        val testDate = LocalDate.of(2024, 1, 1)
        viewModel.onDateSelect(testDate)

        assertEquals(testDate, viewModel.tempSelectedDate)
    }

    @Test
    fun onConfirmDate_shouldFilterMoodsByDate() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        val testDate = LocalDate.of(2024, 1, 1)
        viewModel.onDateSelect(testDate)
        viewModel.onConfirmDate()

        assertEquals(testDate, viewModel.selectedDate)
        assertFalse(viewModel.showDatePicker)
        assertEquals(1, viewModel.uiState.value.moods.size)
    }

    @Test
    fun onClearDate_shouldShowAllMoods() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        val testDate = LocalDate.of(2024, 1, 1)
        viewModel.onDateSelect(testDate)
        viewModel.onConfirmDate()
        viewModel.onClearDate()

        assertNull(viewModel.selectedDate)
        assertNull(viewModel.tempSelectedDate)
        assertEquals(3, viewModel.uiState.value.moods.size)
    }

    @Test
    fun onShowDatePicker_shouldSetShowDatePickerTrue() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        viewModel.onShowDatePicker()

        assertTrue(viewModel.showDatePicker)
    }

    @Test
    fun onDismissDatePicker_shouldResetDatePickerState() = runTest {
        viewModel = MoodHistoryViewModel(moodRepository)

        val testDate = LocalDate.of(2024, 1, 1)
        viewModel.onDateSelect(testDate)
        viewModel.onShowDatePicker()
        viewModel.onDismissDatePicker()

        assertFalse(viewModel.showDatePicker)
        assertNull(viewModel.tempSelectedDate)
    }
}