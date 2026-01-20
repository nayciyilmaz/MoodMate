package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.HomeUiState
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentMoods()
    }

    fun loadRecentMoods() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            when (val result = moodRepository.getUserMoods()) {
                is Resource.Success -> {
                    val recentMoods = result.data?.take(3) ?: emptyList()
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        moods = recentMoods
                    )
                }
                is Resource.Error -> {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = HomeUiState(isLoading = true)
                }
            }
        }
    }
}