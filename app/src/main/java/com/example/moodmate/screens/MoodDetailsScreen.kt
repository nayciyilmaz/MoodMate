package com.example.moodmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.components.EditScaffold

@Composable
fun MoodDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    EditScaffold(navController = navController) {
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Detay Sayfası")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoodDetailsScreenPreview() {
    MoodDetailsScreen(navController = rememberNavController())
}