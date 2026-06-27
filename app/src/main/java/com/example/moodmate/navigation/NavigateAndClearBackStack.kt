package com.example.moodmate.navigation

import androidx.navigation.NavController

fun navigateAndClearBackStack(
    navController: NavController,
    destination: String,
    popUpToRoute: String,
    inclusive: Boolean = true
) {
    navController.navigate(destination) {
        popUpTo(popUpToRoute) {
            this.inclusive = inclusive
        }
    }
}