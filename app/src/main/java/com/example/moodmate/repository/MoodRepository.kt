package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.mapper.toResponse
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()

        return Resource.Success(entity.toResponse())
    }

    suspend fun updateMood(
        moodId: Long,
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        val existing = moodDao.getMoodByServerId(moodId)
            ?: return Resource.Error(context.getString(R.string.error_unknown))

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

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()

        return Resource.Success(updated.toResponse())
    }

    suspend fun deleteMood(moodId: Long): Resource<Unit> {
        val existing = moodDao.getMoodByServerId(moodId)
            ?: return Resource.Error(context.getString(R.string.error_unknown))

        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            moodDao.deleteMoodByLocalId(existing.localId)
        } else {
            moodDao.updateSyncStatus(existing.localId, SyncStatus.PENDING_DELETE)
        }

        val pendingCount = moodDao.getPendingMoods().size
        syncManager.updatePendingState(pendingCount)
        syncScheduler.scheduleSync()

        return Resource.Success(Unit)
    }

    suspend fun getUserMoods(): Resource<List<MoodResponse>> {
        val userId = tokenManager.userId.first() ?: return Resource.Error(
            context.getString(R.string.error_unknown)
        )
        val moods = moodDao.getMoods(userId).map { it.toResponse() }
        return Resource.Success(moods)
    }

    suspend fun clearAllMoodsForUser() {
        val userId = tokenManager.userId.first() ?: return
        moodDao.deleteAllMoodsForUser(userId)
    }
}