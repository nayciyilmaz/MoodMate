package com.example.moodmate.presentation.screens.add_mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.presentation.components.DateTimeSection
import com.example.moodmate.presentation.components.EditDetailsButton
import com.example.moodmate.presentation.components.EditScaffold
import com.example.moodmate.presentation.components.MoodSelector
import com.example.moodmate.presentation.components.NoteSection
import com.example.moodmate.presentation.components.RatingSelector
import com.example.moodmate.presentation.components.ValidationErrorText
import com.example.moodmate.presentation.components.EditDatePicker
import com.example.moodmate.presentation.components.EditTimePicker
import com.example.moodmate.presentation.navigation.MoodMateScreens
import com.example.moodmate.presentation.navigation.navigateAndClearBackStack
import com.example.moodmate.presentation.screens.add_mood.AddMoodViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddMoodScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddMoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(key1 = actionState.isSuccess) {
        if (actionState.isSuccess) {
            navigateAndClearBackStack(
                navController = navController,
                destination = MoodMateScreens.HomeScreen.route,
                popUpToRoute = MoodMateScreens.AddMoodScreen.route
            )
        }
    }

    val currentMonth = uiState.currentMonth?.let { YearMonth.parse(it) } ?: YearMonth.now()
    val tempSelectedDate = uiState.tempSelectedDate?.let { LocalDate.parse(it) }

    if (uiState.showDatePicker) {
        EditDatePicker(
            currentMonth = currentMonth,
            tempSelectedDate = tempSelectedDate,
            onMonthChange = viewModel::onMonthChange,
            onDateSelect = viewModel::onTempDateSelect,
            onDismiss = viewModel::onDismissDatePicker,
            onConfirm = viewModel::onConfirmDate
        )
    }

    if (uiState.showTimePicker) {
        val currentTime = uiState.selectedTime?.let {
            LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
        } ?: LocalTime.now()

        EditTimePicker(
            initialTime = currentTime,
            onDismiss = viewModel::onDismissTimePicker,
            onConfirm = viewModel::onConfirmTime
        )
    }

    EditScaffold(
        title = stringResource(id = R.string.title_add_mood),
        navController = navController
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoodSelector(
                    moods = viewModel.moods,
                    selectedMoodIndex = uiState.selectedMoodIndex,
                    onMoodSelected = viewModel::onMoodSelected
                )

                RatingSelector(
                    selectedRating = uiState.selectedRating,
                    onRatingSelected = viewModel::onRatingSelected
                )

                NoteSection(
                    noteText = uiState.noteText,
                    onNoteChanged = viewModel::onNoteTextChange
                )

                DateTimeSection(
                    date = uiState.selectedDate?.let {
                        LocalDate.parse(it).format(
                            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
                        )
                    } ?: "",
                    time = uiState.selectedTime ?: "",
                    onDateClick = viewModel::onShowDatePicker,
                    onTimeClick = viewModel::onShowTimePicker
                )

                uiState.validationError?.let { error ->
                    ValidationErrorText(error = error)
                }
            }

            EditDetailsButton(
                text = stringResource(id = R.string.save_button),
                icon = Icons.Default.CheckCircle,
                onClick = viewModel::saveMood,
                containerColor = colorResource(id = R.color.acik_mavi),
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMoodScreenPreview() {
    AddMoodScreen(navController = rememberNavController())
}
