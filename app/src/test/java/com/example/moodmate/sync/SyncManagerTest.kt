package com.example.moodmate.sync

import com.example.moodmate.dao.MoodDao
import com.example.moodmate.data.MoodResponse
import com.example.moodmate.entity.MoodEntity
import com.example.moodmate.local.TokenManager
import com.example.moodmate.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private lateinit var moodDao: MoodDao
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var syncManager: SyncManager

    private val testDispatcher = UnconfinedTestDispatcher()

    private val syncedMoodEntity = MoodEntity(
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

    private val pendingCreateEntity = syncedMoodEntity.copy(
        localId = "local-create",
        serverId = null,
        syncStatus = SyncStatus.PENDING_CREATE
    )

    private val pendingUpdateEntity = syncedMoodEntity.copy(
        localId = "local-update",
        syncStatus = SyncStatus.PENDING_UPDATE
    )

    private val pendingDeleteEntity = syncedMoodEntity.copy(
        localId = "local-delete",
        syncStatus = SyncStatus.PENDING_DELETE
    )

    private val serverMoodResponse = MoodResponse(
        id = 1L,
        emoji = "😊",
        score = 8,
        note = "Great day",
        entryDate = "2024-02-21T10:00:00",
        createdAt = "2024-02-21T10:00:00"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        moodDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)

        coEvery { tokenManager.userId } returns flowOf(1L)
        coEvery { moodDao.getPendingMoods() } returns emptyList()
        coEvery { moodDao.getMoods(1L) } returns emptyList()
        coEvery { apiService.getUserMoods() } returns Response.success(emptyList())

        syncManager = SyncManager(moodDao, apiService, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updatePendingState_shouldUpdateSyncState() {
        syncManager.updatePendingState(3)

        assertTrue(syncManager.syncState.value is SyncState.PendingOffline)
        assertEquals(3, (syncManager.syncState.value as SyncState.PendingOffline).count)
    }

    @Test
    fun sync_whenNoPendingMoods_shouldSetSyncedState() = runTest {
        coEvery { apiService.getUserMoods() } returns Response.success(emptyList())

        syncManager.sync()

        assertTrue(syncManager.syncState.value is SyncState.Synced)
    }

    @Test
    fun sync_whenPendingCreateSuccess_shouldUpdateServerIdAndStatus() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingCreateEntity) andThen emptyList()
        coEvery { apiService.addMood(any()) } returns Response.success(serverMoodResponse)
        coEvery { apiService.getUserMoods() } returns Response.success(listOf(serverMoodResponse))

        syncManager.sync()

        coVerify {
            moodDao.updateServerIdAndStatus(
                pendingCreateEntity.localId,
                serverMoodResponse.id,
                SyncStatus.SYNCED
            )
        }
    }

    @Test
    fun sync_whenPendingCreateFails_shouldSetSyncFailedState() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingCreateEntity)
        coEvery { apiService.addMood(any()) } throws Exception("Network error")

        syncManager.sync()

        assertTrue(syncManager.syncState.value is SyncState.SyncFailed)
    }

    @Test
    fun sync_whenPendingUpdateSuccess_shouldUpdateStatus() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingUpdateEntity) andThen emptyList()
        coEvery { apiService.updateMood(any(), any()) } returns Response.success(serverMoodResponse)
        coEvery { apiService.getUserMoods() } returns Response.success(listOf(serverMoodResponse))

        syncManager.sync()

        coVerify { moodDao.updateSyncStatus(pendingUpdateEntity.localId, SyncStatus.SYNCED) }
    }

    @Test
    fun sync_whenPendingUpdateFails_shouldSetSyncFailedState() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingUpdateEntity)
        coEvery { apiService.updateMood(any(), any()) } throws Exception("Network error")

        syncManager.sync()

        assertTrue(syncManager.syncState.value is SyncState.SyncFailed)
    }

    @Test
    fun sync_whenPendingDeleteSuccess_shouldDeleteFromLocalDb() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingDeleteEntity) andThen emptyList()
        coEvery { apiService.deleteMood(any()) } returns Response.success(null)
        coEvery { apiService.getUserMoods() } returns Response.success(emptyList())

        syncManager.sync()

        coVerify { moodDao.deleteMoodByLocalId(pendingDeleteEntity.localId) }
    }

    @Test
    fun sync_whenPendingDeleteFails_shouldSetSyncFailedState() = runTest {
        coEvery { moodDao.getPendingMoods() } returns listOf(pendingDeleteEntity)
        coEvery { apiService.deleteMood(any()) } throws Exception("Network error")

        syncManager.sync()

        assertTrue(syncManager.syncState.value is SyncState.SyncFailed)
    }

    @Test
    fun sync_whenServerHasNewMood_shouldInsertToLocalDb() = runTest {
        val newServerMood = serverMoodResponse.copy(id = 99L)
        coEvery { apiService.getUserMoods() } returns Response.success(listOf(newServerMood))
        coEvery { moodDao.getMoodByServerId(99L) } returns null

        syncManager.sync()

        coVerify { moodDao.insertMood(match { it.serverId == 99L }) }
    }

    @Test
    fun sync_whenLocalMoodDeletedFromServer_shouldDeleteFromLocalDb() = runTest {
        coEvery { moodDao.getMoods(1L) } returns listOf(syncedMoodEntity)
        coEvery { apiService.getUserMoods() } returns Response.success(emptyList())

        syncManager.sync()

        coVerify { moodDao.deleteMoodByLocalId(syncedMoodEntity.localId) }
    }

    @Test
    fun sync_whenPendingDeleteHasNoServerId_shouldDeleteDirectlyFromLocalDb() = runTest {
        val noServerIdDelete = pendingDeleteEntity.copy(serverId = null)
        coEvery { moodDao.getPendingMoods() } returns listOf(noServerIdDelete) andThen emptyList()
        coEvery { apiService.getUserMoods() } returns Response.success(emptyList())

        syncManager.sync()

        coVerify { moodDao.deleteMoodByLocalId(noServerIdDelete.localId) }
    }
}