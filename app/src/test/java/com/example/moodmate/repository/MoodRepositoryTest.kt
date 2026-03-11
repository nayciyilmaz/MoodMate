package com.example.moodmate.repository

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.dao.MoodDao
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.sync.SyncManager
import com.example.moodmate.sync.SyncScheduler
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MoodRepositoryTest {

    private lateinit var moodDao: MoodDao
    private lateinit var syncManager: SyncManager
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var tokenManager: TokenManager
    private lateinit var context: Context
    private lateinit var repository: MoodRepository

    private val testMoodEntity = MoodEntity(
        localId = "local-123",
        serverId = 1L,
        userId = 1L,
        emoji = "😊",
        score = 8,
        note = "Great day",
        entryDate = "2024-02-21T10:00:00",
        createdAt = "2024-02-21T10:00:00",
        updatedAt = "2024-02-21T10:00:00",
        syncStatus = SyncStatus.SYNCED
    )

    @Before
    fun setUp() {
        moodDao = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_unknown) } returns "Unknown error"

        coEvery { tokenManager.userId } returns flowOf(1L)

        repository = MoodRepository(moodDao, syncManager, syncScheduler, tokenManager, context)
    }

    @Test
    fun addMood_shouldInsertToLocalDbWithPendingCreateStatus() = runTest {
        val result = repository.addMood("😊", 8, "Great day", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Success)
        coVerify {
            moodDao.insertMood(
                match { it.syncStatus == SyncStatus.PENDING_CREATE && it.emoji == "😊" }
            )
        }
    }

    @Test
    fun addMood_shouldScheduleSync() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(testMoodEntity)

        repository.addMood("😊", 8, "Great day", "2024-02-21T10:00:00")

        coVerify { syncScheduler.scheduleSync() }
    }

    @Test
    fun addMood_shouldUpdatePendingState() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(testMoodEntity)

        repository.addMood("😊", 8, "Great day", "2024-02-21T10:00:00")

        coVerify { syncManager.updatePendingState(1) }
    }

    @Test
    fun updateMood_whenMoodExists_shouldUpdateWithPendingUpdateStatus() = runTest {
        coEvery { moodDao.getMoodByServerId(1L) } returns testMoodEntity

        val result = repository.updateMood(1L, "😔", 5, "Updated note", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Success)
        coVerify {
            moodDao.updateMood(
                match { it.syncStatus == SyncStatus.PENDING_UPDATE && it.emoji == "😔" }
            )
        }
    }

    @Test
    fun updateMood_whenMoodNotFound_shouldReturnError() = runTest {
        coEvery { moodDao.getMoodByServerId(1L) } returns null

        val result = repository.updateMood(1L, "😔", 5, "Updated note", "2024-02-21T10:00:00")

        assertTrue(result is Resource.Error)
    }

    @Test
    fun updateMood_whenMoodIsPendingCreate_shouldKeepPendingCreateStatus() = runTest {
        val pendingCreateMood = testMoodEntity.copy(syncStatus = SyncStatus.PENDING_CREATE)
        coEvery { moodDao.getMoodByServerId(1L) } returns pendingCreateMood

        repository.updateMood(1L, "😔", 5, "Updated note", "2024-02-21T10:00:00")

        coVerify {
            moodDao.updateMood(
                match { it.syncStatus == SyncStatus.PENDING_CREATE }
            )
        }
    }

    @Test
    fun deleteMood_whenMoodIsSynced_shouldMarkAsPendingDelete() = runTest {
        coEvery { moodDao.getMoodByServerId(1L) } returns testMoodEntity

        val result = repository.deleteMood(1L)

        assertTrue(result is Resource.Success)
        coVerify { moodDao.updateSyncStatus(testMoodEntity.localId, SyncStatus.PENDING_DELETE) }
    }

    @Test
    fun deleteMood_whenMoodIsPendingCreate_shouldDeleteDirectly() = runTest {
        val pendingCreateMood = testMoodEntity.copy(syncStatus = SyncStatus.PENDING_CREATE)
        coEvery { moodDao.getMoodByServerId(1L) } returns pendingCreateMood

        val result = repository.deleteMood(1L)

        assertTrue(result is Resource.Success)
        coVerify { moodDao.deleteMoodByLocalId(pendingCreateMood.localId) }
    }

    @Test
    fun deleteMood_whenMoodNotFound_shouldReturnError() = runTest {
        coEvery { moodDao.getMoodByServerId(1L) } returns null

        val result = repository.deleteMood(1L)

        assertTrue(result is Resource.Error)
    }

    @Test
    fun getUserMoods_shouldReturnMoodsFromLocalDb() = runTest {
        coEvery { moodDao.getMoods(1L) } returns listOf(testMoodEntity)

        val result = repository.getUserMoods()

        assertTrue(result is Resource.Success)
        assertEquals(1, (result as Resource.Success).data?.size)
        assertEquals("😊", result.data?.first()?.emoji)
    }

    @Test
    fun getUserMoods_whenDbEmpty_shouldReturnEmptyList() = runTest {
        coEvery { moodDao.getMoods(1L) } returns emptyList()

        val result = repository.getUserMoods()

        assertTrue(result is Resource.Success)
        assertEquals(0, (result as Resource.Success).data?.size)
    }

    @Test
    fun clearAllMoodsForUser_shouldCallDeleteAllForUser() = runTest {
        repository.clearAllMoodsForUser()

        coVerify { moodDao.deleteAllMoodsForUser(1L) }
    }
}