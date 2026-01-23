package com.example.moodmate.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.moodmate.data.MoodResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class MoodDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _moodDetails = MutableStateFlow<MoodResponse?>(null)
    val moodDetails: StateFlow<MoodResponse?> = _moodDetails.asStateFlow()

    init {
        val moodJson = savedStateHandle.get<String>("moodJson")
        moodJson?.let {
            val decodedJson = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            val gson = Gson()
            _moodDetails.value = gson.fromJson(decodedJson, MoodResponse::class.java)
        }
    }
}