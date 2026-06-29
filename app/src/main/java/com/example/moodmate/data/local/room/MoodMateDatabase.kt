package com.example.moodmate.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moodmate.data.local.Converters

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
