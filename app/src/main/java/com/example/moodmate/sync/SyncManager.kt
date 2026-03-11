package com.example.moodmate.sync

import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.MoodRequest
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.mapper.toEntity
import com.example.moodmate.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
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
        val userId = tokenManager.userId.first() ?: run {
            Timber.e("Sync başlatılamadı: userId bulunamadı")
            return
        }
        val pendingMoods = moodDao.getPendingMoods()
        Timber.d("Sync başlatıldı: userId=$userId, bekleyen kayıt sayısı=${pendingMoods.size}")

        if (pendingMoods.isNotEmpty()) {
            _syncState.value = SyncState.Syncing
        }

        val success = processPendingMoods(pendingMoods)

        if (pendingMoods.isNotEmpty() && !success) {
            val remaining = moodDao.getPendingMoods()
            Timber.e("Sync başarısız: ${remaining.size} kayıt gönderilemedi")
            _syncState.value = SyncState.SyncFailed(remaining.size)
            return
        }

        fetchAndMergeFromServer(userId)

        val remaining = moodDao.getPendingMoods()
        if (remaining.isEmpty()) {
            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            Timber.d("Sync tamamlandı: userId=$userId, saat=$now")
            _syncState.value = SyncState.Synced(now)
        } else {
            Timber.e("Sync tamamlanamadı: ${remaining.size} kayıt kaldı")
            _syncState.value = SyncState.SyncFailed(remaining.size)
        }
    }

    fun updatePendingState(count: Int) {
        Timber.d("Bekleyen kayıt durumu güncellendi: $count kayıt")
        _syncState.value = SyncState.PendingOffline(count)
    }

    private suspend fun processPendingMoods(pendingMoods: List<MoodEntity>): Boolean {
        var allSuccess = true
        for (mood in pendingMoods) {
            Timber.d("İşleniyor: localId=${mood.localId}, syncStatus=${mood.syncStatus}")
            val result = when (mood.syncStatus) {
                SyncStatus.PENDING_CREATE -> syncCreate(mood)
                SyncStatus.PENDING_UPDATE -> syncUpdate(mood)
                SyncStatus.PENDING_DELETE -> syncDelete(mood)
                SyncStatus.SYNCED -> true
            }
            if (!result) {
                Timber.e("İşlem başarısız: localId=${mood.localId}, syncStatus=${mood.syncStatus}")
                allSuccess = false
            }
        }
        return allSuccess
    }

    private suspend fun syncCreate(mood: MoodEntity): Boolean {
        return try {
            Timber.d("Create sync: localId=${mood.localId}")
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
                    Timber.d("Create sync başarılı: localId=${mood.localId}, serverId=${serverMood.id}")
                }
                true
            } else {
                Timber.e("Create sync başarısız: localId=${mood.localId}, kod=${response.code()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Create sync hatası: localId=${mood.localId}")
            false
        }
    }

    private suspend fun syncUpdate(mood: MoodEntity): Boolean {
        val serverId = mood.serverId ?: run {
            Timber.e("Update sync başarısız: serverId bulunamadı, localId=${mood.localId}")
            return false
        }
        return try {
            Timber.d("Update sync: localId=${mood.localId}, serverId=$serverId")
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
                Timber.d("Update sync başarılı: localId=${mood.localId}, serverId=$serverId")
                true
            } else {
                Timber.e("Update sync başarısız: localId=${mood.localId}, kod=${response.code()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Update sync hatası: localId=${mood.localId}")
            false
        }
    }

    private suspend fun syncDelete(mood: MoodEntity): Boolean {
        val serverId = mood.serverId ?: run {
            moodDao.deleteMoodByLocalId(mood.localId)
            Timber.d("Delete sync: serverId yok, direkt silindi: localId=${mood.localId}")
            return true
        }
        return try {
            Timber.d("Delete sync: localId=${mood.localId}, serverId=$serverId")
            val response = apiService.deleteMood(serverId)
            if (response.isSuccessful) {
                moodDao.deleteMoodByLocalId(mood.localId)
                Timber.d("Delete sync başarılı: localId=${mood.localId}, serverId=$serverId")
                true
            } else {
                Timber.e("Delete sync başarısız: localId=${mood.localId}, kod=${response.code()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Delete sync hatası: localId=${mood.localId}")
            false
        }
    }

    private suspend fun fetchAndMergeFromServer(userId: Long) {
        try {
            Timber.d("Sunucudan moodlar çekiliyor: userId=$userId")
            val response = apiService.getUserMoods()
            if (!response.isSuccessful) {
                Timber.e("Sunucudan moodlar çekilemedi: kod=${response.code()}")
                return
            }
            val serverMoods = response.body() ?: return
            val localMoods = moodDao.getMoods(userId)
            Timber.d("Sunucudan ${serverMoods.size} mood alındı, local'de ${localMoods.size} mood var")

            for (serverMood in serverMoods) {
                val existing = moodDao.getMoodByServerId(serverMood.id)
                if (existing == null) {
                    moodDao.insertMood(serverMood.toEntity(userId))
                    Timber.d("Yeni mood eklendi: serverId=${serverMood.id}")
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
                        Timber.d("Mood güncellendi sunucudan: serverId=${serverMood.id}")
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
                    Timber.d("Sunucuda olmayan mood silindi: localId=${localMood.localId}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Sunucudan moodlar çekilirken hata: userId=$userId")
            return
        }
    }

    private fun parseDateTime(dateStr: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Timber.e("Tarih parse hatası: $dateStr")
            null
        }
    }
}