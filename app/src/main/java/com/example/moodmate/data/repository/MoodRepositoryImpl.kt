package com.example.moodmate.data.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.local.datastore.TokenManager
import com.example.moodmate.data.local.room.MoodDao
import com.example.moodmate.data.local.room.MoodEntity
import com.example.moodmate.data.mapper.toResponse
import com.example.moodmate.domain.model.MoodResponse
import com.example.moodmate.domain.repository.MoodRepository
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao,
    private val syncManager: SyncManager,
    private val syncScheduler: SyncScheduler,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : MoodRepository {

    override suspend fun observeMoods(): Flow<List<MoodResponse>> {
        val userId = tokenManager.userId.first() ?: 0L
        return moodDao.observeMoods(userId).map { entities -> entities.map { it.toResponse() } }
    }

    override suspend fun addMood(
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        val userId = tokenManager.userId.first()
            ?: return Resource.Error(context.getString(R.string.error_unknown))

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val entity = MoodEntity(
            localId = UUID.randomUUID().toString(),
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
        syncManager.updatePendingState(moodDao.getPendingMoods().size)
        syncScheduler.scheduleSync()

        return Resource.Success(entity.toResponse())
    }

    override suspend fun updateMood(
        moodId: Long,
        emoji: String,
        score: Int,
        note: String,
        entryDate: String
    ): Resource<MoodResponse> {
        val existing = moodDao.getMoodByServerId(moodId)
            ?: return Resource.Error(context.getString(R.string.error_unknown))

        val updated = existing.copy(
            emoji = emoji,
            score = score,
            note = note,
            entryDate = entryDate,
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            syncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE)
                SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE
        )

        moodDao.updateMood(updated)
        syncManager.updatePendingState(moodDao.getPendingMoods().size)
        syncScheduler.scheduleSync()

        return Resource.Success(updated.toResponse())
    }

    override suspend fun deleteMood(moodId: Long): Resource<Unit> {
        val existing = moodDao.getMoodByServerId(moodId)
            ?: return Resource.Error(context.getString(R.string.error_unknown))

        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            moodDao.deleteMoodByLocalId(existing.localId)
        } else {
            moodDao.updateSyncStatus(existing.localId, SyncStatus.PENDING_DELETE)
        }

        syncManager.updatePendingState(moodDao.getPendingMoods().size)
        syncScheduler.scheduleSync()

        return Resource.Success(Unit)
    }

    override suspend fun getUserMoods(): Resource<List<MoodResponse>> {
        val userId = tokenManager.userId.first()
            ?: return Resource.Error(context.getString(R.string.error_unknown))
        return Resource.Success(moodDao.getMoods(userId).map { it.toResponse() })
    }

    override suspend fun clearAllMoodsForUser() {
        val userId = tokenManager.userId.first() ?: return
        moodDao.deleteAllMoodsForUser(userId)
    }
}
