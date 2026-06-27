package com.example.moodmate.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.moodmate.R
import com.example.moodmate.navigation.MoodMateScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopBar(
    navController: NavController,
    currentRoute: String?
) {
    val title = when (currentRoute) {
        MoodMateScreens.AddMoodScreen.route -> stringResource(R.string.title_add_mood)
        MoodMateScreens.UpdateMoodScreen.route -> stringResource(R.string.title_edit_mood)
        MoodMateScreens.HomeScreen.route -> stringResource(R.string.title_home)
        MoodMateScreens.MoodHistoryScreen.route -> stringResource(R.string.title_mood_history)
        MoodMateScreens.MoodDetailsScreen.route -> stringResource(R.string.title_mood_details)
        MoodMateScreens.ProfileScreen.route -> stringResource(R.string.title_profile)
        MoodMateScreens.SettingsScreen.route -> stringResource(R.string.title_settings)
        else -> ""
    }

    val showBackButton = currentRoute == MoodMateScreens.MoodDetailsScreen.route ||
            currentRoute == MoodMateScreens.UpdateMoodScreen.route

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                EditIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    onClick = { navController.popBackStack() }
                )
            }
        }
    )
}
