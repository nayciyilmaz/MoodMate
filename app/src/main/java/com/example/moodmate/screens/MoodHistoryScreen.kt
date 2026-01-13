package com.example.moodmate.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.components.EditTextButton
import com.example.moodmate.components.EditTextField
import com.example.moodmate.viewmodel.MoodHistoryViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodHistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MoodHistoryViewModel = hiltViewModel()
) {
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
                enabled = false
            )

            if (viewModel.showDatePicker) {
                CustomDatePickerDialog(
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
        }
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun CustomDatePickerDialog(
    currentMonth: YearMonth,
    tempSelectedDate: LocalDate?,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weekDays = context.resources.getStringArray(R.array.week_days_short)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = modifier.padding(16.dp)
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(id = R.color.background_blue),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditTextButton(
                            text = "<",
                            onClick = {
                                onMonthChange(currentMonth.minusMonths(1))
                            }
                        )

                        Text(
                            text = currentMonth.format(
                                DateTimeFormatter.ofPattern(
                                    "MMMM yyyy",
                                    Locale("tr")
                                )
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )

                        EditTextButton(
                            text = ">",
                            onClick = {
                                onMonthChange(currentMonth.plusMonths(1))
                            }
                        )
                    }
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorResource(id = R.color.acik_mavi)
                        )
                    }
                }

                CalendarDaysGrid(
                    currentMonth = currentMonth,
                    tempSelectedDate = tempSelectedDate,
                    onDateSelect = onDateSelect,
                    modifier = modifier
                )

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    EditTextButton(
                        text = stringResource(id = R.string.mood_history_cancel),
                        onClick = onDismiss
                    )

                    EditTextButton(
                        text = stringResource(id = R.string.mood_history_ok),
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    currentMonth: YearMonth,
    tempSelectedDate: LocalDate?,
    onDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        val daysInMonth = currentMonth.lengthOfMonth()

        var dayCounter = 1
        for (week in 0..5) {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 1..7) {
                    val dayToShow =
                        if (week == 0 && dayOfWeek < firstDayOfWeek) {
                            null
                        } else if (dayCounter > daysInMonth) {
                            null
                        } else {
                            dayCounter++
                        }

                    Box(
                        modifier = modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                color = if (dayToShow != null && tempSelectedDate?.dayOfMonth == dayToShow && tempSelectedDate?.month == currentMonth.month) {
                                    colorResource(id = R.color.acik_mavi)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = dayToShow != null) {
                                dayToShow?.let {
                                    onDateSelect(currentMonth.atDay(it))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        dayToShow?.let {
                            Text(
                                text = it.toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (tempSelectedDate?.dayOfMonth == it && tempSelectedDate?.month == currentMonth.month) {
                                    Color.White
                                } else {
                                    Color.Black
                                }
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
fun MoodHistoryScreenPreview() {
    MoodHistoryScreen(navController = rememberNavController())
}