package com.example.moodmate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moodmate.R
import com.example.moodmate.navigation.MoodMateScreens

@Composable
fun EditScaffold(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != MoodMateScreens.MoodDetailsScreen.route &&
            currentRoute != MoodMateScreens.SplashScreen.route &&
            currentRoute != MoodMateScreens.SignInScreen.route &&
            currentRoute != MoodMateScreens.SignUpScreen.route

    val showTopBar = currentRoute != MoodMateScreens.SplashScreen.route &&
            currentRoute != MoodMateScreens.SignInScreen.route &&
            currentRoute != MoodMateScreens.SignUpScreen.route

    Scaffold(
        topBar = {
            if (showTopBar) {
                EditTopBar(navController = navController, currentRoute = currentRoute)
            }
        },
        bottomBar = {
            if (showBottomBar) {
                EditBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background_blue))
                .padding(paddingValues)
        ) {
            content(PaddingValues())
        }
    }
}