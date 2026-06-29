package com.example.moodmate.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advice")
data class AdviceLocalEntity(
    @PrimaryKey
    val userId: Long,
    val serverId: Long,
    val advice: String,
    val createdAt: String
)
