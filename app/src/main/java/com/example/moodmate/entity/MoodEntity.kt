package com.example.moodmate.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moodmate.sync.SyncStatus

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey
    val localId: String,
    val serverId: Long?,
    val userId: Long,
    val emoji: String,
    val score: Int,
    val note: String,
    val entryDate: String,
    val createdAt: String,
    val updatedAt: String,
    val syncStatus: SyncStatus
)