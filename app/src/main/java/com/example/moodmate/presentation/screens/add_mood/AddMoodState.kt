package com.example.moodmate.presentation.screens.add_mood

data class AddMoodState(
    val selectedMoodIndex: Int = -1,
    val selectedRating: Int = -1,
    val noteText: String = "",
    val selectedDate: String? = null,
    val selectedTime: String? = null,
    val showDatePicker: Boolean = false,
    val currentMonth: String? = null,
    val tempSelectedDate: String? = null,
    val showTimePicker: Boolean = false,
    val validationError: String? = null
) {
    fun isValid(): Boolean {
        return selectedMoodIndex >= 0 &&
                selectedRating > 0 &&
                noteText.isNotBlank()
    }
}

data class AddMoodActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class MoodItem(
    val emoji: String,
    val label: String
)
