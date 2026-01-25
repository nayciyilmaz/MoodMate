package com.example.moodmate.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moodmate.R
import com.example.moodmate.navigation.MoodMateScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopBar(
    navController: NavController,
    currentRoute: String?
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val moodId = navBackStackEntry?.arguments?.getString("moodId")
    val isEditMode = currentRoute?.startsWith("add_mood_screen") == true && moodId != null

    val title = when {
        isEditMode -> stringResource(R.string.title_edit_mood)
        currentRoute?.startsWith("add_mood_screen") == true -> stringResource(R.string.title_add_mood)
        currentRoute == MoodMateScreens.HomeScreen.route -> stringResource(R.string.title_home)
        currentRoute == MoodMateScreens.MoodHistoryScreen.route -> stringResource(R.string.title_mood_history)
        currentRoute == MoodMateScreens.MoodDetailsScreen.route -> stringResource(R.string.title_mood_details)
        currentRoute == MoodMateScreens.ProfileScreen.route -> stringResource(R.string.title_profile)
        else -> ""
    }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (currentRoute == MoodMateScreens.MoodDetailsScreen.route) {
                EditIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    onClick = { navController.popBackStack() }
                )
            }
        }
    )
}