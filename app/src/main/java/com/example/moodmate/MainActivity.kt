package com.example.moodmate

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moodmate.navigation.MoodMateNavigation
import com.example.moodmate.notification.NotificationPermissionManager
import com.example.moodmate.ui.theme.MoodMateTheme
import com.example.moodmate.util.LocaleHelper
import com.example.moodmate.util.WindowConfigHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationPermissionManager: NotificationPermissionManager

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        WindowConfigHelper.setupTransparentBars(window)

        notificationPermissionManager.initialize(this)
        notificationPermissionManager.requestNotificationPermission(this)

        setContent {
            MoodMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    MoodMateNavigation()
                }
            }
        }
    }
}