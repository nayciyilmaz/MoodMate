package com.example.moodmate.navigation

enum class MoodMateScreens(val route: String) {
    SplashScreen("splash_screen"),
    SignInScreen("sign_in_screen"),
    SignUpScreen("sign_up_screen"),
    HomeScreen("home_screen"),
    AddMoodScreen("add_mood_screen?moodId={moodId}"),
    MoodHistoryScreen("mood_history_screen"),
    MoodDetailsScreen("mood_details_screen/{moodJson}"),
    ProfileScreen("profile_screen");

    companion object {
        fun createMoodDetailsRoute(moodJson: String): String {
            return "mood_details_screen/$moodJson"
        }

        fun createAddMoodRoute(moodId: Long? = null): String {
            return if (moodId != null) {
                "add_mood_screen?moodId=$moodId"
            } else {
                "add_mood_screen"
            }
        }
    }
}