package com.example.moodmate.entity

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