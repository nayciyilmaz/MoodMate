package com.example.moodmate.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.moodmate.util.NotificationPreferenceHelper
import javax.inject.Inject

class NotificationPermissionManager @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    fun initialize(activity: ComponentActivity) {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                NotificationHelper.createNotificationChannel(activity)
                if (NotificationPreferenceHelper.isNotificationEnabled(activity)) {
                    notificationScheduler.scheduleDailyNotifications()
                }
            }
        }
    }

    fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationHelper.createNotificationChannel(context)
                    if (NotificationPreferenceHelper.isNotificationEnabled(context)) {
                        notificationScheduler.scheduleDailyNotifications()
                    }
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationHelper.createNotificationChannel(context)
            if (NotificationPreferenceHelper.isNotificationEnabled(context)) {
                notificationScheduler.scheduleDailyNotifications()
            }
        }
    }
}