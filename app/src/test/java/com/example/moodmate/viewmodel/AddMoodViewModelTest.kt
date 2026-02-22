package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.moodmate.R
import com.example.moodmate.data.MoodResponse
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddMoodViewModelTest {

    private lateinit var viewModel: AddMoodViewModel
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

        every { savedStateHandle.get<String>("moodId") } returns null

        viewModel = AddMoodViewModel(moodRepository, context, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onMoodSelected_shouldUpdateSelectedMoodIndexAndClearError() {
        viewModel.onMoodSelected(1)

        assertEquals(1, viewModel.uiState.value.selectedMoodIndex)
        assertNull(viewModel.uiState.value.validationError)
    }

    @Test
    fun onRatingSelected_shouldUpdateSelectedRatingAndClearError() {
        viewModel.onRatingSelected(8)

        assertEquals(8, viewModel.uiState.value.selectedRating)
        assertNull(viewModel.uiState.value.validationError)
    }

    @Test
    fun onNoteTextChange_shouldUpdateNoteTextAndClearError() {
        viewModel.onNoteTextChange("Test note")

        assertEquals("Test note", viewModel.uiState.value.noteText)
        assertNull(viewModel.uiState.value.validationError)
    }

    @Test
    fun saveMood_whenFieldsInvalid_shouldShowValidationError() = runTest {
        viewModel.saveMood()

        assertEquals("All fields required", viewModel.uiState.value.validationError)
        assertFalse(viewModel.actionState.value.isSuccess)
    }

    @Test
    fun saveMood_whenAddingNewMood_shouldCallAddMoodAndSetSuccess() = runTest {
        val moodResponse = MoodResponse(
            id = 1L,
            emoji = "😊",
            score = 8,
            note = "Great day",
            entryDate = "2024-01-01T00:00:00",
            createdAt = "2024-01-01T00:00:00"
        )
        coEvery { moodRepository.addMood(any(), any(), any(), any()) } returns Resource.Success(moodResponse)

        viewModel.onMoodSelected(0)
        viewModel.onRatingSelected(8)
        viewModel.onNoteTextChange("Great day")
        viewModel.saveMood()

        assertTrue(viewModel.actionState.value.isSuccess)
        assertFalse(viewModel.actionState.value.isLoading)
        coVerify { moodRepository.addMood("😊", 8, "Great day", any()) }
    }

    @Test
    fun saveMood_whenUpdatingMood_shouldCallUpdateMood() = runTest {
        every { savedStateHandle.get<String>("moodId") } returns "1"
        viewModel = AddMoodViewModel(moodRepository, context, savedStateHandle)

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

        assertTrue(viewModel.isEditMode)
        assertTrue(viewModel.actionState.value.isSuccess)
        coVerify { moodRepository.updateMood(1L, "😔", 5, "Updated note", any()) }
    }

    @Test
    fun saveMood_whenError_shouldShowErrorMessage() = runTest {
        coEvery { moodRepository.addMood(any(), any(), any(), any()) } returns Resource.Error("Network error")

        viewModel.onMoodSelected(0)
        viewModel.onRatingSelected(8)
        viewModel.onNoteTextChange("Test")
        viewModel.saveMood()

        assertFalse(viewModel.actionState.value.isSuccess)
        assertEquals("Network error", viewModel.actionState.value.error)
    }

    @Test
    fun setInitialData_whenEditMode_shouldPopulateFields() {
        every { savedStateHandle.get<String>("moodId") } returns "1"
        viewModel = AddMoodViewModel(moodRepository, context, savedStateHandle)

        viewModel.setInitialData("😊", 8, "Test note")

        assertEquals(0, viewModel.uiState.value.selectedMoodIndex)
        assertEquals(8, viewModel.uiState.value.selectedRating)
        assertEquals("Test note", viewModel.uiState.value.noteText)
    }
}