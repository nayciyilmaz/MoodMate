package com.example.moodmate.util

import androidx.navigation.NavController

fun navigateAndClearBackStack(
    navController: NavController,
    destination: String,
    popUpToRoute: String
) {
    navController.navigate(destination) {
        popUpTo(popUpToRoute) {
            inclusive = true
        }
    }
}