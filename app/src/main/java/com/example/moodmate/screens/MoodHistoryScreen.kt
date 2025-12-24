package com.example.moodmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.components.EditScaffold

@Composable
fun MoodHistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Geçmiş Sayfası")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoodHistoryScreenPreview() {
    MoodHistoryScreen(navController = rememberNavController())
}