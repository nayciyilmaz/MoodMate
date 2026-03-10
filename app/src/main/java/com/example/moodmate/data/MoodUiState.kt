package com.example.moodmate.data

data class AddMoodUiState(
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

data class HomeUiState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null,
    val showSessionExpiredDialog: Boolean = false
)

data class MoodHistoryUiState(
    val isLoading: Boolean = false,
    val moods: List<MoodResponse> = emptyList(),
    val error: String? = null
)

data class MoodDetailsUiState(
    val moodDetails: MoodResponse? = null,
    val showDeleteDialog: Boolean = false,
    val deleteSuccess: Boolean = false
)