package com.example.moodmate.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM moods WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY entryDate DESC")
    fun observeMoods(userId: Long): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY entryDate DESC")
    suspend fun getMoods(userId: Long): List<MoodEntity>

    @Query("SELECT * FROM moods WHERE serverId = :serverId LIMIT 1")
    suspend fun getMoodByServerId(serverId: Long): MoodEntity?

    @Query("SELECT * FROM moods WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingMoods(): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntity>)

    @Update
    suspend fun updateMood(mood: MoodEntity)

    @Query("UPDATE moods SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: String, status: SyncStatus)

    @Query("UPDATE moods SET serverId = :serverId, syncStatus = :status WHERE localId = :localId")
    suspend fun updateServerIdAndStatus(localId: String, serverId: Long, status: SyncStatus)

    @Query("DELETE FROM moods WHERE localId = :localId")
    suspend fun deleteMoodByLocalId(localId: String)

    @Query("DELETE FROM moods WHERE userId = :userId")
    suspend fun deleteAllMoodsForUser(userId: Long)
}