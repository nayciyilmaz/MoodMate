package com.example.moodmate.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class MoodDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _moodDetails = MutableStateFlow<MoodResponse?>(null)
    val moodDetails: StateFlow<MoodResponse?> = _moodDetails.asStateFlow()

    private var currentMoodId: Long = 0L

    init {
        val moodJson = savedStateHandle.get<String>("moodJson")
        moodJson?.let {
            val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            val gson = Gson()
            val mood = gson.fromJson(decodedJson, MoodResponse::class.java)
            _moodDetails.value = mood
            currentMoodId = mood.id
        }
    }

    fun refreshMoodDetails() {
        viewModelScope.launch {
            when (val result = moodRepository.getUserMoods()) {
                is Resource.Success -> {
                    val updatedMood = result.data?.find { it.id == currentMoodId }
                    _moodDetails.value = updatedMood
                }
                else -> { }
            }
        }
    }
}