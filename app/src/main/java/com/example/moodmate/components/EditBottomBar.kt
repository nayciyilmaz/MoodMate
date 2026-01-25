package com.example.moodmate.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.moodmate.R
import com.example.moodmate.data.NavigationItem
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.util.navigateAndClearBackStack

@Composable
fun EditBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    val acikMavi = colorResource(id = R.color.acik_mavi)

    val navigationItems = listOf(
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

    NavigationBar(containerColor = Color.White) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navigateAndClearBackStack(
                        navController = navController,
                        destination = item.route,
                        popUpToRoute = MoodMateScreens.HomeScreen.route,
                        inclusive = item.route == MoodMateScreens.HomeScreen.route
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(id = item.labelResId))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = acikMavi,
                    selectedTextColor = acikMavi,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = acikMavi.copy(alpha = 0.1f)
                )
            )
        }
    }
}