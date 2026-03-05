package com.example.moodmate.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditDatePicker
import com.example.moodmate.components.EditIconButton
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.components.EditTextField
import com.example.moodmate.components.MoodList
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.viewmodel.MoodHistoryViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodHistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MoodHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("shouldRefresh")?.observeForever { shouldRefresh ->
            if (shouldRefresh == true) {
                viewModel.loadMoods()
                navController.currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", false)
            }
        }
    }

    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            EditTextField(
                value = viewModel.searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = modifier.fillMaxWidth(),
                placeholder = stringResource(id = R.string.mood_history_search_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (viewModel.searchText.isNotEmpty()) {
                        EditIconButton(
                            icon = Icons.Default.Clear,
                            contentDescription = null,
                            onClick = { viewModel.onClearSearch() }
                        )
                    }
                }
            )

            EditTextField(
                value = viewModel.selectedDate?.format(
                    DateTimeFormatter.ofPattern(
                        "dd MMMM yyyy",
                        Locale("tr")
                    )
                ) ?: "",
                onValueChange = {},
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { viewModel.onShowDatePicker() },
                placeholder = stringResource(id = R.string.mood_history_date_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (viewModel.selectedDate != null) {
                        EditIconButton(
                            icon = Icons.Default.Clear,
                            contentDescription = null,
                            onClick = { viewModel.onClearDate() }
                        )
                    }
                },
                enabled = false
            )

            if (viewModel.showDatePicker) {
                EditDatePicker(
                    currentMonth = viewModel.currentMonth,
                    tempSelectedDate = viewModel.tempSelectedDate,
                    onMonthChange = viewModel::onMonthChange,
                    onDateSelect = viewModel::onDateSelect,
                    onDismiss = viewModel::onDismissDatePicker,
                    onConfirm = viewModel::onConfirmDate,
                    modifier = modifier
                )
            }

            Text(
                text = stringResource(id = R.string.past_moods),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(bottom = 20.dp)
            )

            MoodList(
                isLoading = uiState.isLoading,
                moods = uiState.moods,
                onMoodClick = { mood ->
                    val gson = Gson()
                    val moodJson = gson.toJson(mood)
                    val encodedMoodJson = URLEncoder.encode(moodJson, StandardCharsets.UTF_8.toString())
                        .replace("+", "%20")
                    navController.navigate(MoodMateScreens.createMoodDetailsRoute(encodedMoodJson))
                },
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MoodHistoryScreenPreview() {
    MoodHistoryScreen(navController = rememberNavController())
}