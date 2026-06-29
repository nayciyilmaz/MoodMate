package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.local.room.MoodDao
import com.example.moodmate.model.MoodResponse
import com.example.moodmate.data.local.room.MoodEntity
import com.example.moodmate.data.local.datastore.TokenManager
import com.example.moodmate.data.mapper.toResponse
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val moodDao: MoodDao,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {
    suspend fun observeMoods(): Flow<List<MoodResponse>> {
        val userId = tokenManager.userId.first() ?: 0L
        Timber.d("Mood akışı dinleniyor: userId=$userId")
        return moodDao.observeMoods(userId).map { entities ->
            entities.map { it.toResponse() }
        }
    }

    suspend fun addMood(
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )

        Timber.d("Mood ekleniyor: userId=$userId, emoji=$emoji, score=$score")

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val localId = UUID.randomUUID().toString()

        val entity = MoodEntity(
            localId = localId,
            serverId = null,
            userId = userId,
            emoji = emoji,
            score = score,
            note = note,
            entryDate = entryDate,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        moodDao.insertMood(entity)
        Timber.d("Mood local DB'ye eklendi: localId=$localId")

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()
        Timber.d("Sync planlandı: bekleyen kayıt sayısı=$pendingCount")

        return Resource.Success(entity.toResponse())
    }

    suspend fun updateMood(
        moodId: Long,
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        val existing = moodDao.getMoodByServerId(moodId) ?: run {
            Timber.e("Güncellenecek mood bulunamadı: moodId=$moodId")
            return Resource.Error(context.getString(R.string.error_unknown))
        }

        Timber.d("Mood güncelleniyor: moodId=$moodId, emoji=$emoji, score=$score")

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val updated = existing.copy(
            emoji = emoji,
            score = score,
            note = note,
            entryDate = entryDate,
            updatedAt = now,
            syncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE)
                SyncStatus.PENDING_CREATE
            else
                SyncStatus.PENDING_UPDATE
        )

        moodDao.updateMood(updated)
        Timber.d("Mood güncellendi: localId=${existing.localId}, syncStatus=${updated.syncStatus}")

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()
        Timber.d("Sync planlandı: bekleyen kayıt sayısı=$pendingCount")

        return Resource.Success(updated.toResponse())
    }

    suspend fun deleteMood(moodId: Long): Resource<Unit> {
        val existing = moodDao.getMoodByServerId(moodId) ?: run {
            Timber.e("Silinecek mood bulunamadı: moodId=$moodId")
            return Resource.Error(context.getString(R.string.error_unknown))
        }

        Timber.d("Mood siliniyor: moodId=$moodId, syncStatus=${existing.syncStatus}")

        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            moodDao.deleteMoodByLocalId(existing.localId)
            Timber.d("Mood direkt silindi: localId=${existing.localId}")
        } else {
            moodDao.updateSyncStatus(existing.localId, SyncStatus.PENDING_DELETE)
            Timber.d("Mood silinmek üzere işaretlendi: localId=${existing.localId}")
        }

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()
        Timber.d("Sync planlandı: bekleyen kayıt sayısı=$pendingCount")

        return Resource.Success(Unit)
    }

    suspend fun getUserMoods(): Resource<List<MoodResponse>> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        Timber.d("Moodlar getiriliyor: userId=$userId")
        val moods = moodDao.getMoods(userId).map { it.toResponse() }
        Timber.d("Moodlar getirildi: ${moods.size} kayıt")
        return Resource.Success(moods)
    }

    suspend fun clearAllMoodsForUser() {
        val userId = tokenManager.userId.first() ?: return
        Timber.d("Tüm moodlar siliniyor: userId=$userId")
        moodDao.deleteAllMoodsForUser(userId)
        Timber.d("Tüm moodlar silindi: userId=$userId")
    }
}