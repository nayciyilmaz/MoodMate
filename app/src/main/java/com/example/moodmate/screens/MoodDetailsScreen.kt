package com.example.moodmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditDetailsButton
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.util.formatDate
import com.example.moodmate.viewmodel.MoodDetailsViewModel

@Composable
fun MoodDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MoodDetailsViewModel = hiltViewModel()
) {
    val moodDetails by viewModel.moodDetails.collectAsState()

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("shouldRefresh")?.observeForever { shouldRefresh ->
            if (shouldRefresh == true) {
                viewModel.refreshMoodDetails()
                navController.currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", false)

                try {
                    navController.getBackStackEntry(MoodMateScreens.HomeScreen.route)
                        .savedStateHandle.set("shouldRefresh", true)
                } catch (e: Exception) { }

                try {
                    navController.getBackStackEntry(MoodMateScreens.MoodHistoryScreen.route)
                        .savedStateHandle.set("shouldRefresh", true)
                } catch (e: Exception) { }
            }
        }
    }

    EditScaffold(navController = navController) { innerPadding ->
        moodDetails?.let { mood ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MoodEmojiCard(
                        emoji = mood.emoji,
                        dateTime = formatDate(mood.entryDate),
                        score = mood.score,
                        modifier = modifier
                    )

                    DetailedNoteCard(
                        note = mood.note,
                        modifier = modifier
                    )
                }

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditDetailsButton(
                        text = stringResource(id = R.string.edit),
                        icon = Icons.Default.Edit,
                        onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("emoji", mood.emoji)
                            navController.currentBackStackEntry?.savedStateHandle?.set("score", mood.score)
                            navController.currentBackStackEntry?.savedStateHandle?.set("note", mood.note)
                            navController.navigate(MoodMateScreens.createAddMoodRoute(mood.id))
                        },
                        containerColor = colorResource(id = R.color.acik_mavi)
                    )

                    EditDetailsButton(
                        text = stringResource(id = R.string.delete),
                        icon = Icons.Default.Delete,
                        onClick = { },
                        containerColor = Color.Red
                    )

                    EditDetailsButton(
                        text = stringResource(id = R.string.ask_ai),
                        icon = Icons.Default.AutoAwesome,
                        onClick = { },
                        containerColor = colorResource(id = R.color.ai_purple)
                    )
                }
            }
        }
    }
}

@Composable
fun MoodEmojiCard(
    emoji: String,
    dateTime: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )

            Surface(
                color = colorResource(id = R.color.light_gray_background),
                shape = RoundedCornerShape(12.dp),
                modifier = modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$score / 10",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.date_time_color)
                    )
                }
            }

            Surface(
                color = colorResource(id = R.color.light_gray_background),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.date_time_color)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedNoteCard(
    note: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    color = colorResource(id = R.color.success_green).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "📝",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.padding(8.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.detailed_note),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoodDetailsScreenPreview() {
    MoodDetailsScreen(navController = rememberNavController())
}