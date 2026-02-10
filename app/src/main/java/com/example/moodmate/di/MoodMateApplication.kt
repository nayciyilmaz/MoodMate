package com.example.moodmate.di

import android.app.Application
import android.content.Context
import com.example.moodmate.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoodMateApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val languageCode = LocaleHelper.getLanguage(base)
        super.attachBaseContext(LocaleHelper.setLocale(base, languageCode))
    }
}