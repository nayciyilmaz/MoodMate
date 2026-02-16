package com.example.moodmate.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.moodmate.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_TYPE_KEY = "notification_type"
        const val MORNING_NOTIFICATION = "morning"
        const val EVENING_NOTIFICATION = "evening"
        const val MORNING_NOTIFICATION_ID = 1001
        const val EVENING_NOTIFICATION_ID = 1002
    }

    override suspend fun doWork(): Result {
        return try {
            val notificationType = inputData.getString(NOTIFICATION_TYPE_KEY)

            when (notificationType) {
                MORNING_NOTIFICATION -> {
                    NotificationHelper.showNotification(
                        context = context,
                        title = context.getString(R.string.app_name),
                        message = context.getString(R.string.notification_morning_message),
                        notificationId = MORNING_NOTIFICATION_ID
                    )
                }
                EVENING_NOTIFICATION -> {
                    NotificationHelper.showNotification(
                        context = context,
                        title = context.getString(R.string.app_name),
                        message = context.getString(R.string.notification_evening_message),
                        notificationId = EVENING_NOTIFICATION_ID
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}