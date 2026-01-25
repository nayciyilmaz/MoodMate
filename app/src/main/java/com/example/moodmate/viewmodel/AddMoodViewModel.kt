package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.AddMoodActionState
import com.example.moodmate.data.AddMoodUiState
import com.example.moodmate.data.MoodItem
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddMoodViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMoodUiState())
    val uiState: StateFlow<AddMoodUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(AddMoodActionState())
    val actionState: StateFlow<AddMoodActionState> = _actionState.asStateFlow()

    val moods: List<MoodItem> = context.resources.getStringArray(R.array.mood_list).map {
        val parts = it.split("-")
        MoodItem(emoji = parts[0], label = parts[1])
    }

    private val moodIdString: String? = savedStateHandle.get<String>("moodId")
    val isEditMode = !moodIdString.isNullOrEmpty()
    private val moodId: Long = moodIdString?.toLongOrNull() ?: 0L

    fun setInitialData(emoji: String, score: Int, note: String) {
        if (isEditMode) {
            val moodIndex = moods.indexOfFirst { it.emoji == emoji }
            _uiState.value = _uiState.value.copy(
                selectedMoodIndex = moodIndex,
                selectedRating = score,
                noteText = note
            )
        }
    }

    fun onNoteTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(
            noteText = newText,
            validationError = null
        )
    }

    fun onMoodSelected(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedMoodIndex = index,
            validationError = null
        )
    }

    fun onRatingSelected(rating: Int) {
        _uiState.value = _uiState.value.copy(
            selectedRating = rating,
            validationError = null
        )
    }

    fun saveMood() {
        if (!_uiState.value.isValid()) {
            _uiState.value = _uiState.value.copy(
                validationError = context.getString(R.string.error_fields_required)
            )
            return
        }

        viewModelScope.launch {
            _actionState.value = AddMoodActionState(isLoading = true)

            val selectedEmoji = moods[_uiState.value.selectedMoodIndex].emoji
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val entryDate = currentDateTime.format(formatter)

            val result = if (isEditMode) {
                moodRepository.updateMood(
                    moodId = moodId,
                    emoji = selectedEmoji,
                    score = _uiState.value.selectedRating,
                    note = _uiState.value.noteText.trim(),
                    entryDate = entryDate
                )
            } else {
                moodRepository.addMood(
                    emoji = selectedEmoji,
                    score = _uiState.value.selectedRating,
                    note = _uiState.value.noteText.trim(),
                    entryDate = entryDate
                )
            }

            when (result) {
                is Resource.Success -> {
                    _actionState.value = AddMoodActionState(isSuccess = true)
                    if (!isEditMode) {
                        resetForm()
                    }
                }
                is Resource.Error -> {
                    _actionState.value = AddMoodActionState(
                        error = result.message ?: context.getString(R.string.error_save_mood_failed)
                    )
                }
                is Resource.Loading -> {
                    _actionState.value = AddMoodActionState(isLoading = true)
                }
            }
        }
    }

    private fun resetForm() {
        _uiState.value = AddMoodUiState()
    }

    fun resetActionState() {
        _actionState.value = AddMoodActionState()
    }
}