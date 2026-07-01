package com.example.moodmate.di

import com.example.moodmate.data.repository.AdviceRepositoryImpl
import com.example.moodmate.data.repository.AuthRepositoryImpl
import com.example.moodmate.data.repository.MoodRepositoryImpl
import com.example.moodmate.domain.repository.AdviceRepository
import com.example.moodmate.domain.repository.AuthRepository
import com.example.moodmate.domain.repository.MoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository

    @Binds
    @Singleton
    abstract fun bindAdviceRepository(impl: AdviceRepositoryImpl): AdviceRepository
}
