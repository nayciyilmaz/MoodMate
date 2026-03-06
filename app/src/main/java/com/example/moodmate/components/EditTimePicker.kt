package com.example.moodmate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.moodmate.R
import java.time.LocalTime

@Composable
fun EditTimePicker(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableIntStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.select_time_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.acik_mavi),
                    modifier = modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(id = R.color.background_blue),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.acik_mavi)
                    )
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.hour_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = modifier.padding(bottom = 8.dp)
                        )

                        ScrollWheel(
                            items = (0..23).toList(),
                            selectedItem = selectedHour,
                            onItemSelected = { selectedHour = it }
                        )
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.acik_mavi)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.minute_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = modifier.padding(bottom = 8.dp)
                        )

                        ScrollWheel(
                            items = (0..59).toList(),
                            selectedItem = selectedMinute,
                            onItemSelected = { selectedMinute = it }
                        )
                    }
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    EditTextButton(
                        text = stringResource(id = R.string.mood_history_cancel),
                        onClick = onDismiss
                    )

                    EditTextButton(
                        text = stringResource(id = R.string.mood_history_ok),
                        onClick = {
                            onConfirm(LocalTime.of(selectedHour, selectedMinute))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollWheel(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        val targetIndex = (selectedItem - 2).coerceAtLeast(0)
        listState.animateScrollToItem(targetIndex)
    }

    Box(
        modifier = modifier
            .width(80.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.background_blue))
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = item == selectedItem

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) colorResource(id = R.color.acik_mavi)
                            else Color.Transparent
                        )
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", item),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isSelected) 22.sp else 18.sp,
                        color = if (isSelected) Color.White else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}