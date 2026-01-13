package com.example.moodmate.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddMoodViewModel @Inject constructor() : ViewModel() {

    var selectedMoodIndex by mutableIntStateOf(-1)
    var selectedRating by mutableIntStateOf(-1)

    var noteText by mutableStateOf("")
        private set

    fun onNoteTextChange(newText: String) {
        noteText = newText
    }

    fun onMoodSelected(index: Int) {
        selectedMoodIndex = index
    }

    fun onRatingSelected(rating: Int) {
        selectedRating = rating
    }
}