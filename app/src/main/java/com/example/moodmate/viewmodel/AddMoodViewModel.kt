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
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
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

    fun setInitialData(emoji: String, score: Int, note: String, entryDate: String? = null) {
        val moodIndex = moods.indexOfFirst { it.emoji == emoji }

        if (isEditMode && entryDate != null) {
            try {
                val dateTime = java.time.LocalDateTime.parse(entryDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val dateStr = dateTime.toLocalDate().toString()
                val timeStr = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                _uiState.value = _uiState.value.copy(
                    selectedMoodIndex = moodIndex,
                    selectedRating = score,
                    noteText = note,
                    selectedDate = dateStr,
                    selectedTime = timeStr,
                    currentMonth = YearMonth.from(dateTime.toLocalDate()).toString()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    selectedMoodIndex = moodIndex,
                    selectedRating = score,
                    noteText = note
                )
            }
        } else if (isEditMode) {
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

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date.toString(),
            validationError = null
        )
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            selectedTime = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            validationError = null
        )
    }

    fun onShowDatePicker() {
        val today = LocalDate.now()
        _uiState.value = _uiState.value.copy(
            showDatePicker = true,
            tempSelectedDate = _uiState.value.selectedDate ?: today.toString(),
            currentMonth = _uiState.value.currentMonth ?: YearMonth.now().toString()
        )
    }

    fun onDismissDatePicker() {
        _uiState.value = _uiState.value.copy(
            showDatePicker = false,
            tempSelectedDate = null
        )
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = month.toString())
    }

    fun onTempDateSelect(date: LocalDate) {
        _uiState.value = _uiState.value.copy(tempSelectedDate = date.toString())
    }

    fun onConfirmDate() {
        val tempDate = _uiState.value.tempSelectedDate
        if (tempDate != null) {
            _uiState.value = _uiState.value.copy(
                selectedDate = tempDate,
                showDatePicker = false,
                tempSelectedDate = null,
                validationError = null
            )
        }
    }

    fun onShowTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = true)
    }

    fun onDismissTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = false)
    }

    fun onConfirmTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(
            selectedTime = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            showTimePicker = false,
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
            val entryDate = buildEntryDate()

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

    private fun buildEntryDate(): String {
        val state = _uiState.value
        val date = if (state.selectedDate != null) {
            LocalDate.parse(state.selectedDate)
        } else {
            LocalDate.now()
        }

        val time = if (state.selectedTime != null) {
            LocalTime.parse(state.selectedTime, DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            LocalTime.now()
        }

        val dateTime = date.atTime(time)
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun resetForm() {
        _uiState.value = AddMoodUiState()
    }

    fun resetActionState() {
        _actionState.value = AddMoodActionState()
    }
}