package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.domain.model.AddMoodActionState
import com.example.moodmate.domain.model.AddMoodUiState
import com.example.moodmate.domain.model.MoodItem
import com.example.moodmate.domain.repository.MoodRepository
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMoodUiState())
    val uiState: StateFlow<AddMoodUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(AddMoodActionState())
    val actionState: StateFlow<AddMoodActionState> = _actionState.asStateFlow()

    val moods: List<MoodItem> = context.resources.getStringArray(R.array.mood_list).map {
        val parts = it.split("-")
        MoodItem(emoji = parts[0], label = parts[1])
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

            val result = moodRepository.addMood(
                emoji = selectedEmoji,
                score = _uiState.value.selectedRating,
                note = _uiState.value.noteText.trim(),
                entryDate = entryDate
            )

            when (result) {
                is Resource.Success -> {
                    _actionState.value = AddMoodActionState(isSuccess = true)
                    resetForm()
                }
                is Resource.Error -> _actionState.value = AddMoodActionState(
                    error = result.message ?: context.getString(R.string.error_save_mood_failed)
                )
                is Resource.Loading -> _actionState.value = AddMoodActionState(isLoading = true)
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

        return date.atTime(time).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun resetForm() {
        _uiState.value = AddMoodUiState()
    }

    fun resetActionState() {
        _actionState.value = AddMoodActionState()
    }
}
