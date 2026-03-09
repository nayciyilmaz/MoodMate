package com.example.moodmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.AdviceUiState
import com.example.moodmate.data.HomeUiState
import com.example.moodmate.data.SyncState
import com.example.moodmate.local.TokenManager
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.util.NetworkMonitor
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val adviceRepository: AdviceRepository,
    private val tokenManager: TokenManager,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler,
    private val networkMonitor: NetworkMonitor,
    private val moodDao: MoodDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _adviceState = MutableStateFlow(AdviceUiState())
    val adviceState: StateFlow<AdviceUiState> = _adviceState.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    init {
        observeMoods()
        observeAdvice()
        observeNetwork()
        triggerInitialSync()
    }

    private fun observeMoods() {
        viewModelScope.launch {
            moodRepository.observeMoods().collect { moods ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    moods = moods.take(3)
                )
            }
        }
    }

    private fun observeAdvice() {
        viewModelScope.launch {
            adviceRepository.observeAdvice().collect { advice ->
                if (advice != null) {
                    _adviceState.value = AdviceUiState(
                        advice = advice.advice,
                        createdAt = advice.createdAt
                    )
                }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    syncScheduler.scheduleSync()
                }
            }
        }
    }

    private fun triggerInitialSync() {
        viewModelScope.launch {
            val isValid = tokenManager.isTokenValid()
            if (!isValid) {
                tokenManager.clearUser()
                _uiState.value = _uiState.value.copy(showSessionExpiredDialog = true)
                return@launch
            }
            val isOnline = networkMonitor.isOnline.first()
            if (!isOnline) {
                val pendingCount = moodDao.getPendingMoods().size
                syncManager.updatePendingState(pendingCount)
                return@launch
            }
            val pendingCount = moodDao.getPendingMoods().size
            if (pendingCount > 0) {
                syncManager.updatePendingState(pendingCount)
            }
            syncScheduler.scheduleSync()
        }
    }

    fun loadRecentMoods() {
        viewModelScope.launch {
            val result = moodRepository.getUserMoods()
            if (result is Resource.Error && result.isUnauthorized) {
                _uiState.value = _uiState.value.copy(showSessionExpiredDialog = true)
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
                        _uiState.value = _uiState.value.copy(showSessionExpiredDialog = true)
                    }
                    _adviceState.value = _adviceState.value.copy(
                        isLoading = false,
                        error = result.message ?: context.getString(R.string.error_ai_unavailable)
                    )
                }
                else -> {}
            }
        }
    }

    fun navigateToLoginAfterSessionExpiry() {
        _shouldNavigateToLogin.value = true
    }

    fun resetNavigationFlag() {
        _shouldNavigateToLogin.value = false
    }
}