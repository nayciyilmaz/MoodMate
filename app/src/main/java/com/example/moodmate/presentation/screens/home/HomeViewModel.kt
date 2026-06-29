package com.example.moodmate.presentation.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodmate.R
import com.example.moodmate.data.local.datastore.TokenManager
import com.example.moodmate.data.local.room.MoodDao
import com.example.moodmate.domain.repository.AdviceRepository
import com.example.moodmate.domain.repository.MoodRepository
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.sync.SyncState
import com.example.moodmate.util.NetworkMonitor
import com.example.moodmate.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _adviceState = MutableStateFlow(AdviceState())
    val adviceState: StateFlow<AdviceState> = _adviceState.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow(false)
    val shouldNavigateToLogin: StateFlow<Boolean> = _shouldNavigateToLogin.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    val firstName: StateFlow<String> = tokenManager.firstName
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), "")

    init {
        observeMoods()
        observeAdvice()
        observeNetwork()
        triggerInitialSync()
    }

    private fun observeMoods() {
        viewModelScope.launch {
            moodRepository.observeMoods().collect { moods ->
                Timber.Forest.d("Moodlar güncellendi: ${moods.size} kayıt")
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
                    Timber.Forest.d("Tavsiye güncellendi")
                    _adviceState.value = AdviceState(
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
                Timber.Forest.d("Ağ durumu değişti: isOnline=$isOnline")
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
                Timber.Forest.w("Token geçersiz, oturum süresi doldu")
                tokenManager.clearUser()
                _uiState.value = _uiState.value.copy(showSessionExpiredDialog = true)
                return@launch
            }
            val isOnline = networkMonitor.isOnline.first()
            if (!isOnline) {
                val pendingCount = moodDao.getPendingMoods().size
                Timber.Forest.d("Çevrimdışı mod: bekleyen kayıt=$pendingCount")
                syncManager.updatePendingState(pendingCount)
                return@launch
            }
            val pendingCount = moodDao.getPendingMoods().size
            if (pendingCount > 0) {
                Timber.Forest.d("Bekleyen kayıtlar var: $pendingCount")
                syncManager.updatePendingState(pendingCount)
            }
            syncScheduler.scheduleSync()
        }
    }

    fun loadRecentMoods() {
        viewModelScope.launch {
            Timber.Forest.d("Son moodlar yükleniyor")
            val result = moodRepository.getUserMoods()
            if (result is Resource.Error && result.isUnauthorized) {
                Timber.Forest.e("Oturum süresi doldu, login ekranına yönlendiriliyor")
                _uiState.value = _uiState.value.copy(showSessionExpiredDialog = true)
            }
        }
    }

    fun generateAdvice() {
        viewModelScope.launch {
            Timber.Forest.d("Tavsiye oluşturuluyor")
            _adviceState.value = _adviceState.value.copy(isLoading = true, error = null)
            when (val result = adviceRepository.generateAdvice()) {
                is Resource.Success -> {
                    Timber.Forest.d("Tavsiye başarıyla oluşturuldu")
                    result.data?.let {
                        _adviceState.value = AdviceState(
                            advice = it.advice,
                            createdAt = it.createdAt,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    Timber.Forest.e("Tavsiye oluşturma başarısız: ${result.message}")
                    if (result.isUnauthorized) {
                        Timber.Forest.e("Oturum süresi doldu, login ekranına yönlendiriliyor")
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
        Timber.Forest.d("Oturum süresi doldu, login ekranına yönlendiriliyor")
        _shouldNavigateToLogin.value = true
    }

    fun resetNavigationFlag() {
        _shouldNavigateToLogin.value = false
    }
}