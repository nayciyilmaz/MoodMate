package com.example.moodmate.di

import android.content.Context
import androidx.room.Room
import com.example.moodmate.data.local.room.AdviceDao
import com.example.moodmate.data.local.room.MoodDao
import com.example.moodmate.data.local.room.MoodMateDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MoodMateDatabase {
        return Room.databaseBuilder(
            context,
            MoodMateDatabase::class.java,
            "moodmate_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMoodDao(database: MoodMateDatabase): MoodDao = database.moodDao()

    @Provides
    @Singleton
    fun provideAdviceDao(database: MoodMateDatabase): AdviceDao = database.adviceDao()
}
