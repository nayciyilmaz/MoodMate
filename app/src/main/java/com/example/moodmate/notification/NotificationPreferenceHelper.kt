package com.example.moodmate.notification

import android.content.Context

object NotificationPreferenceHelper {

    private const val NOTIFICATION_PREFERENCE = "notification_enabled"
    private const val DEFAULT_NOTIFICATION = true

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(NOTIFICATION_PREFERENCE, enabled).apply()
    }

    fun isNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(NOTIFICATION_PREFERENCE, DEFAULT_NOTIFICATION)
    }
}