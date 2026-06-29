package com.example.moodmate.data.mapper

import com.example.moodmate.domain.model.MoodResponse
import com.example.moodmate.sync.SyncStatus
import com.example.moodmate.data.local.room.MoodEntity
import java.util.UUID

fun MoodResponse.toEntity(userId: Long, syncStatus: SyncStatus = SyncStatus.SYNCED): MoodEntity {
    return MoodEntity(
        localId = UUID.randomUUID().toString(),
        serverId = this.id,
        userId = userId,
        emoji = this.emoji,
        score = this.score,
        note = this.note,
        entryDate = this.entryDate,
        createdAt = this.createdAt,
        updatedAt = this.entryDate,
        syncStatus = syncStatus
    )
}

fun MoodEntity.toResponse(): MoodResponse {
    return MoodResponse(
        id = this.serverId ?: 0L,
        emoji = this.emoji,
        score = this.score,
        note = this.note,
        entryDate = this.entryDate,
        createdAt = this.createdAt
    )
}
