package com.example.moodmate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.example.moodmate.R
import com.example.moodmate.model.NavigationItem

val bottomBarRoutes = setOf(
    MoodMateScreens.HomeScreen.route,
    MoodMateScreens.AddMoodScreen.route,
    MoodMateScreens.MoodHistoryScreen.route,
    MoodMateScreens.ProfileScreen.route
)

val bottomNavItems = listOf(
    NavigationItem(
        route = MoodMateScreens.HomeScreen.route,
        icon = Icons.Default.Home,
        labelResId = R.string.nav_home
    ),
    NavigationItem(
        route = MoodMateScreens.createAddMoodRoute(),
        icon = Icons.Default.Add,
        labelResId = R.string.nav_add
    ),
    NavigationItem(
        route = MoodMateScreens.MoodHistoryScreen.route,
        icon = Icons.Default.History,
        labelResId = R.string.nav_history
    ),
    NavigationItem(
        route = MoodMateScreens.ProfileScreen.route,
        icon = Icons.Default.Person,
        labelResId = R.string.nav_profile
    )
)
