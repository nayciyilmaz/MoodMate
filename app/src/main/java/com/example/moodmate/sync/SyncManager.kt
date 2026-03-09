package com.example.moodmate.sync

import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.MoodRequest
import com.example.moodmate.data.SyncState
import com.example.moodmate.data.SyncStatus
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.mapper.toEntity
import com.example.moodmate.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val moodDao: MoodDao,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun sync() {
        val userId = tokenManager.userId.first() ?: return
        val pendingMoods = moodDao.getPendingMoods()

        if (pendingMoods.isNotEmpty()) {
            _syncState.value = SyncState.Syncing
        }

        val success = processPendingMoods(pendingMoods)

        if (pendingMoods.isNotEmpty() && !success) {
            val remaining = moodDao.getPendingMoods()
            _syncState.value = SyncState.SyncFailed(remaining.size)
            return
        }

        fetchAndMergeFromServer(userId)

        val remaining = moodDao.getPendingMoods()
        if (remaining.isEmpty()) {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            _syncState.value = SyncState.Synced(now)
        } else {
            _syncState.value = SyncState.SyncFailed(remaining.size)
        }
    }

    fun updatePendingState(count: Int) {
        _syncState.value = SyncState.PendingOffline(count)
    }

    private suspend fun processPendingMoods(pendingMoods: List<MoodEntity>): Boolean {
        var allSuccess = true
        for (mood in pendingMoods) {
            val result = when (mood.syncStatus) {
                SyncStatus.PENDING_CREATE -> syncCreate(mood)
                SyncStatus.PENDING_UPDATE -> syncUpdate(mood)
                SyncStatus.PENDING_DELETE -> syncDelete(mood)
                SyncStatus.SYNCED -> true
            }
            if (!result) allSuccess = false
        }
        return allSuccess
    }

    private suspend fun syncCreate(mood: MoodEntity): Boolean {
        return try {
            val response = apiService.addMood(
                MoodRequest(
                    emoji = mood.emoji,
                    score = mood.score,
                    note = mood.note,
                    entryDate = mood.entryDate
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { serverMood ->
                    moodDao.updateServerIdAndStatus(mood.localId, serverMood.id, SyncStatus.SYNCED)
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun syncUpdate(mood: MoodEntity): Boolean {
        val serverId = mood.serverId ?: return false
        return try {
            val response = apiService.updateMood(
                serverId,
                MoodRequest(
                    emoji = mood.emoji,
                    score = mood.score,
                    note = mood.note,
                    entryDate = mood.entryDate
                )
            )
            if (response.isSuccessful) {
                moodDao.updateSyncStatus(mood.localId, SyncStatus.SYNCED)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun syncDelete(mood: MoodEntity): Boolean {
        val serverId = mood.serverId ?: run {
            moodDao.deleteMoodByLocalId(mood.localId)
            return true
        }
        return try {
            val response = apiService.deleteMood(serverId)
            if (response.isSuccessful) {
                moodDao.deleteMoodByLocalId(mood.localId)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchAndMergeFromServer(userId: Long) {
        try {
            val response = apiService.getUserMoods()
            if (!response.isSuccessful) return
            val serverMoods = response.body() ?: return
            val localMoods = moodDao.getMoods(userId)

            for (serverMood in serverMoods) {
                val existing = moodDao.getMoodByServerId(serverMood.id)
                if (existing == null) {
                    moodDao.insertMood(serverMood.toEntity(userId))
                } else {
                    val serverTime = parseDateTime(serverMood.entryDate)
                    val localTime = parseDateTime(existing.updatedAt)
                    if (serverTime != null && localTime != null && serverTime > localTime) {
                        moodDao.insertMood(
                            existing.copy(
                                emoji = serverMood.emoji,
                                score = serverMood.score,
                                note = serverMood.note,
                                entryDate = serverMood.entryDate,
                                updatedAt = serverMood.entryDate,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            }

            val serverIds = serverMoods.map { it.id }.toSet()
            for (localMood in localMoods) {
                if (localMood.serverId != null &&
                    localMood.serverId !in serverIds &&
                    localMood.syncStatus == SyncStatus.SYNCED
                ) {
                    moodDao.deleteMoodByLocalId(localMood.localId)
                }
            }
        } catch (e: Exception) {
            return
        }
    }

    private fun parseDateTime(dateStr: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }
}