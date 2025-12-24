package com.example.moodmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.screens.AddMoodScreen
import com.example.moodmate.screens.HomeScreen
import com.example.moodmate.screens.MoodDetailsScreen
import com.example.moodmate.screens.MoodHistoryScreen
import com.example.moodmate.screens.ProfileScreen
import com.example.moodmate.screens.SignInScreen
import com.example.moodmate.screens.SignUpScreen
import com.example.moodmate.screens.SplashScreen

@Composable
fun MoodMateNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MoodMateScreens.SplashScreen.route) {
        composable(route = MoodMateScreens.SplashScreen.route){
            SplashScreen(navController = navController)
        }
        composable(route = MoodMateScreens.SignInScreen.route){
            SignInScreen(navController = navController)
        }
        composable(route = MoodMateScreens.SignUpScreen.route){
            SignUpScreen(navController = navController)
        }
        composable(route = MoodMateScreens.HomeScreen.route){
            HomeScreen(navController = navController)
        }
        composable(route = MoodMateScreens.AddMoodScreen.route){
            AddMoodScreen(navController = navController)
        }
        composable(route = MoodMateScreens.MoodHistoryScreen.route){
            MoodHistoryScreen(navController = navController)
        }
        composable(route = MoodMateScreens.MoodDetailsScreen.route){
            MoodDetailsScreen(navController = navController)
        }
        composable(route = MoodMateScreens.ProfileScreen.route){
            ProfileScreen(navController = navController)
        }
    }
}