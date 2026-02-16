package com.example.moodmate.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MORNING_WORK_NAME = "morning_notification_work"
        private const val EVENING_WORK_NAME = "evening_notification_work"
    }

    fun scheduleDailyNotifications() {
        scheduleMorningNotification()
        scheduleEveningNotification()
    }

    private fun scheduleMorningNotification() {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val morningWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    NotificationWorker.NOTIFICATION_TYPE_KEY to NotificationWorker.MORNING_NOTIFICATION
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MORNING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            morningWorkRequest
        )
    }

    private fun scheduleEveningNotification() {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val eveningWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    NotificationWorker.NOTIFICATION_TYPE_KEY to NotificationWorker.EVENING_NOTIFICATION
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            eveningWorkRequest
        )
    }

    fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelUniqueWork(MORNING_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_WORK_NAME)
    }
}