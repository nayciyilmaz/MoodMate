package com.example.moodmate.presentation.navigation

enum class MoodMateScreens(val route: String) {
    SplashScreen("splash_screen"),
    SignInScreen("sign_in_screen"),
    SignUpScreen("sign_up_screen"),
    HomeScreen("home_screen"),
    AddMoodScreen("add_mood_screen"),
    UpdateMoodScreen("update_mood_screen/{moodId}"),
    MoodHistoryScreen("mood_history_screen"),
    MoodDetailsScreen("mood_details_screen/{moodJson}"),
    ProfileScreen("profile_screen"),
    SettingsScreen("settings_screen");

    companion object {
        fun createMoodDetailsRoute(moodJson: String): String {
            return "mood_details_screen/$moodJson"
        }

        fun createAddMoodRoute(): String {
            return "add_mood_screen"
        }

        fun createUpdateMoodRoute(moodId: Long): String {
            return "update_mood_screen/$moodId"
        }
    }
}
