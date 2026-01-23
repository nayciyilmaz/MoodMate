package com.example.moodmate.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.MoodHistoryUiState
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MoodHistoryViewModel @Inject constructor(
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodHistoryUiState())
    val uiState: StateFlow<MoodHistoryUiState> = _uiState.asStateFlow()

    var searchText by mutableStateOf("")
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var showDatePicker by mutableStateOf(false)
        private set

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var tempSelectedDate by mutableStateOf<LocalDate?>(null)
        private set

    private var allMoods = listOf<MoodResponse>()

    init {
        loadMoods()
    }

    fun loadMoods() {
        viewModelScope.launch {
            _uiState.value = MoodHistoryUiState(isLoading = true)

            when (val result = moodRepository.getUserMoods()) {
                is Resource.Success -> {
                    allMoods = result.data ?: emptyList()
                    _uiState.value = MoodHistoryUiState(
                        isLoading = false,
                        moods = allMoods
                    )
                }
                is Resource.Error -> {
                    _uiState.value = MoodHistoryUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = MoodHistoryUiState(isLoading = true)
                }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        searchText = text
        filterMoods()
    }

    fun onClearSearch() {
        searchText = ""
        filterMoods()
    }

    fun onShowDatePicker() {
        showDatePicker = true
    }

    fun onDismissDatePicker() {
        showDatePicker = false
        tempSelectedDate = null
    }

    fun onMonthChange(month: YearMonth) {
        currentMonth = month
    }

    fun onDateSelect(date: LocalDate) {
        tempSelectedDate = date
    }

    fun onConfirmDate() {
        tempSelectedDate?.let {
            selectedDate = it
            showDatePicker = false
            filterMoods()
        }
    }

    fun onClearDate() {
        selectedDate = null
        tempSelectedDate = null
        filterMoods()
    }

    private fun filterMoods() {
        val filtered = allMoods.filter { mood ->
            val matchesSearch = searchText.isEmpty() ||
                    mood.note.contains(searchText, ignoreCase = true)

            val matchesDate = selectedDate == null ||
                    mood.entryDate.startsWith(selectedDate.toString())

            matchesSearch && matchesDate
        }

        _uiState.value = MoodHistoryUiState(
            isLoading = false,
            moods = filtered
        )
    }
}