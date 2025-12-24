package com.example.moodmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.moodmate.R
import com.example.moodmate.navigation.MoodMateScreens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {

    val fullText = stringResource(R.string.app_name)
    var animatedText by remember { mutableStateOf("") }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.mood_animation)
    )

    LaunchedEffect(Unit) {
        delay(500)
        fullText.forEachIndexed { index, _ ->
            animatedText = fullText.substring(0, index + 1)
            delay(200)
        }
        delay(1000)
        navController.navigate(MoodMateScreens.SignInScreen.route) {
            popUpTo(MoodMateScreens.SplashScreen.route) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.blue_screen),
                        colorResource(id = R.color.gradient_middle),
                        colorResource(id = R.color.blue_screen)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Text(
            text = animatedText,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF01579B)
            ),
            letterSpacing = 1.5.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}