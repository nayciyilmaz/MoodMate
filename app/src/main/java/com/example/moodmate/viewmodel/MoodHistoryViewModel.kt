package com.example.moodmate.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MoodHistoryViewModel @Inject constructor() : ViewModel() {

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

    fun onSearchTextChange(text: String) {
        searchText = text
    }

    fun onShowDatePicker() {
        showDatePicker = true
    }

    fun onDismissDatePicker() {
        showDatePicker = false
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
        }
    }
}