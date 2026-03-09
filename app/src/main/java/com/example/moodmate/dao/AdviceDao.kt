package com.example.moodmate.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodmate.entity.AdviceLocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdviceDao {

    @Query("SELECT * FROM advice WHERE userId = :userId LIMIT 1")
    fun observeAdvice(userId: Long): Flow<AdviceLocalEntity?>

    @Query("SELECT * FROM advice WHERE userId = :userId LIMIT 1")
    suspend fun getAdvice(userId: Long): AdviceLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvice(advice: AdviceLocalEntity)

    @Query("DELETE FROM advice WHERE userId = :userId")
    suspend fun deleteAdviceForUser(userId: Long)
}