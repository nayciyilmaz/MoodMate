package com.example.moodmate.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.components.EditTextButton
import com.example.moodmate.components.MoodList
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.viewmodel.HomeViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavController,
    firstName: String,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentMoods()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("shouldRefresh")
            ?.observeForever { shouldRefresh ->
                if (shouldRefresh == true) {
                    viewModel.loadRecentMoods()
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "shouldRefresh",
                        false
                    )
                }
            }
    }

    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(id = R.string.welcome_message, firstName),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_greeting_question),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                modifier = modifier.padding(top = 8.dp)
            )

            HomeInfoCard(modifier = modifier)

            AIAdviceCard(modifier = modifier)

            Text(
                text = stringResource(id = R.string.last_mood_entries),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(vertical = 20.dp)
            )

            MoodList(
                isLoading = uiState.isLoading,
                moods = uiState.moods,
                onMoodClick = { mood ->
                    val gson = Gson()
                    val moodJson = gson.toJson(mood)
                    val encodedMoodJson =
                        URLEncoder.encode(moodJson, StandardCharsets.UTF_8.toString())
                            .replace("+", "%20")
                    navController.navigate(MoodMateScreens.createMoodDetailsRoute(encodedMoodJson))
                },
                modifier = modifier
            )
        }
    }
}

@Composable
fun HomeInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = modifier
                .padding(28.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(R.string.home_hope_message),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Text(
                text = "😊",
                fontSize = 48.sp
            )

            Text(
                text = stringResource(R.string.home_reminder_message),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AIAdviceCard(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.microchip),
                        contentDescription = null,
                        modifier = modifier.size(32.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ai_assistant_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.acik_mavi)
                        )
                        Text(
                            text = stringResource(R.string.ai_assistant_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = colorResource(id = R.color.acik_mavi).copy(alpha = 0.4f)
                    )

                    Text(
                        text = "Buraya yapay zekanın yorumu gelecek.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = modifier.padding(vertical = 12.dp)
                    )

                    HorizontalDivider(
                        thickness = 2.dp,
                        color = colorResource(id = R.color.acik_mavi).copy(alpha = 0.4f)
                    )

                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "( 15 Ocak 2025, 14:30 )",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.DarkGray
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditTextButton(
                                text = stringResource(id = R.string.button_close),
                                onClick = { isExpanded = false }
                            )

                            EditTextButton(
                                text = stringResource(id = R.string.button_retry),
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        firstName = "Yılmaz"
    )
}