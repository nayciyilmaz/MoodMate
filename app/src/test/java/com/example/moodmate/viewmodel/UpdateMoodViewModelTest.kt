package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.moodmate.R
import com.example.moodmate.model.MoodResponse
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateMoodViewModelTest {

    private lateinit var viewModel: UpdateMoodViewModel
    private lateinit var moodRepository: MoodRepository
    private lateinit var context: Context
    private lateinit var savedStateHandle: SavedStateHandle

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        moodRepository = mockk()
        context = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        every { context.getString(R.string.error_fields_required) } returns "All fields required"
        every { context.getString(R.string.error_save_mood_failed) } returns "Save failed"
        every { context.resources.getStringArray(R.array.mood_list) } returns arrayOf(
            "😊-Happy",
            "😔-Sad",
            "😃-Excited",
            "😢-Crying"
        )
        every { savedStateHandle.get<String>("moodId") } returns "1"

        viewModel = UpdateMoodViewModel(moodRepository, context, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setInitialData_shouldPopulateFormFields() {
        viewModel.setInitialData("😊", 8, "Test note")

        assertEquals(0, viewModel.uiState.value.selectedMoodIndex)
        assertEquals(8, viewModel.uiState.value.selectedRating)
        assertEquals("Test note", viewModel.uiState.value.noteText)
    }

    @Test
    fun setInitialData_withEntryDate_shouldPopulateDateAndTime() {
        viewModel.setInitialData("😊", 8, "Test note", "2024-01-01T10:30:00")

        assertEquals("2024-01-01", viewModel.uiState.value.selectedDate)
        assertEquals("10:30", viewModel.uiState.value.selectedTime)
    }

    @Test
    fun saveMood_whenFieldsInvalid_shouldShowValidationError() = runTest {
        viewModel.saveMood()

        assertEquals("All fields required", viewModel.uiState.value.validationError)
        assertFalse(viewModel.actionState.value.isSuccess)
    }

    @Test
    fun saveMood_whenFieldsValid_shouldCallUpdateMoodAndSetSuccess() = runTest {
        val moodResponse = MoodResponse(
            id = 1L,
            emoji = "😔",
            score = 5,
            note = "Updated note",
            entryDate = "2024-01-01T00:00:00",
            createdAt = "2024-01-01T00:00:00"
        )
        coEvery { moodRepository.updateMood(any(), any(), any(), any(), any()) } returns Resource.Success(moodResponse)

        viewModel.onMoodSelected(1)
        viewModel.onRatingSelected(5)
        viewModel.onNoteTextChange("Updated note")
        viewModel.saveMood()

        assertTrue(viewModel.actionState.value.isSuccess)
        assertFalse(viewModel.actionState.value.isLoading)
        coVerify { moodRepository.updateMood(1L, "😔", 5, "Updated note", any()) }
    }

    @Test
    fun saveMood_whenError_shouldShowErrorMessage() = runTest {
        coEvery { moodRepository.updateMood(any(), any(), any(), any(), any()) } returns Resource.Error("Network error")

        viewModel.onMoodSelected(0)
        viewModel.onRatingSelected(8)
        viewModel.onNoteTextChange("Test")
        viewModel.saveMood()

        assertFalse(viewModel.actionState.value.isSuccess)
        assertEquals("Network error", viewModel.actionState.value.error)
    }
}
