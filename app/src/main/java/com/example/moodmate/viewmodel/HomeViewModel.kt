package com.example.moodmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.data.AdviceUiState
import com.example.moodmate.data.HomeUiState
import com.example.moodmate.repository.AdviceRepository
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
    private val moodRepository: MoodRepository,
    private val adviceRepository: AdviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _adviceState = MutableStateFlow(AdviceUiState())
    val adviceState: StateFlow<AdviceUiState> = _adviceState.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    init {
        loadRecentMoods()
        loadLatestAdvice()
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
                    if (result.isUnauthorized) {
                        _shouldNavigateToLogin.value = true
                    }
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

    private fun loadLatestAdvice() {
        viewModelScope.launch {
            when (val result = adviceRepository.getLatestAdvice()) {
                is Resource.Success -> {
                    result.data?.let {
                        _adviceState.value = AdviceUiState(
                            advice = it.advice,
                            createdAt = it.createdAt
                        )
                    }
                }
                is Resource.Error -> {
                    if (result.isUnauthorized) {
                        _shouldNavigateToLogin.value = true
                    }
                    _adviceState.value = AdviceUiState()
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun generateAdvice() {
        viewModelScope.launch {
            _adviceState.value = _adviceState.value.copy(isLoading = true, error = null)

            when (val result = adviceRepository.generateAdvice()) {
                is Resource.Success -> {
                    result.data?.let {
                        _adviceState.value = AdviceUiState(
                            advice = it.advice,
                            createdAt = it.createdAt,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    if (result.isUnauthorized) {
                        _shouldNavigateToLogin.value = true
                    }
                    _adviceState.value = _adviceState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Yapay zeka hizmeti şu an kullanılamıyor."
                    )
                }
                is Resource.Loading -> {
                    _adviceState.value = _adviceState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun resetNavigationFlag() {
        _shouldNavigateToLogin.value = false
    }
}