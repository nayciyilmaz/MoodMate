package com.example.moodmate.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moodmate.dao.AdviceDao
import com.example.moodmate.dao.MoodDao
import com.example.moodmate.entity.AdviceLocalEntity
import com.example.moodmate.entity.MoodEntity

@Database(
    entities = [MoodEntity::class, AdviceLocalEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoodMateDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun adviceDao(): AdviceDao
}