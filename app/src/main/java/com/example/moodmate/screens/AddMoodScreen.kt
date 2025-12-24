package com.example.moodmate.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditScaffold
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CheckCircle
import com.example.moodmate.components.EditDetailsButton

@Composable
fun AddMoodScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val moodArray = stringArrayResource(id = R.array.mood_list)
    val moods = remember {
        moodArray.map {
            val parts = it.split("-")
            parts[0] to parts[1]
        }
    }

    var selectedMoodIndex by remember { mutableStateOf(-1) }
    var selectedRating by remember { mutableStateOf(-1) }
    var noteText by remember { mutableStateOf("") }

    EditScaffold(navController = navController) {
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
                    moods = moods,
                    selectedMoodIndex = selectedMoodIndex,
                    onMoodSelected = { selectedMoodIndex = it }
                )

                RatingSelector(
                    selectedRating = selectedRating,
                    onRatingSelected = { selectedRating = it }
                )

                NoteSection(
                    noteText = noteText,
                    onNoteChanged = { noteText = it },
                )
            }

            EditDetailsButton(
                text = stringResource(id = R.string.save_button),
                icon = Icons.Default.CheckCircle,
                onClick = { },
                containerColor = colorResource(id = R.color.acik_mavi)
            )
        }
    }
}

@Composable
fun MoodSelector(
    moods: List<Pair<String, String>>,
    selectedMoodIndex: Int,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = stringResource(id = R.string.mood_select_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(vertical = 16.dp)
        ) {
            items(moods.size) { index ->
                val (emoji, label) = moods[index]
                val isSelected = selectedMoodIndex == index

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "scale"
                )

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) colorResource(id = R.color.acik_mavi) else Color.White,
                    animationSpec = tween(300),
                    label = "backgroundColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            width = if (isSelected) 0.dp else 2.dp,
                            color = if (isSelected) Color.Transparent else colorResource(id = R.color.blue_screen),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onMoodSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun RatingSelector(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = stringResource(id = R.string.rating_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(colorResource(id = R.color.blue_screen))
                    .padding(vertical = 1.dp)
                    .align(Alignment.Center)
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..10) {
                    val isSelected = selectedRating == i

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.3f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "ratingScale"
                    )

                    val color by animateColorAsState(
                        targetValue = if (isSelected) colorResource(id = R.color.acik_mavi) else Color.White,
                        animationSpec = tween(300),
                        label = "ratingColor"
                    )

                    Box(
                        modifier = modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(horizontal = 2.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 2.dp,
                                color = colorResource(id = R.color.blue_screen),
                                shape = CircleShape
                            )
                            .clickable { onRatingSelected(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = i.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteSection(
    noteText: String,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = stringResource(id = R.string.note_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = noteText,
            onValueChange = onNoteChanged,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            placeholder = {
                Text(stringResource(id = R.string.note_placeholder))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = colorResource(id = R.color.blue_screen),
                unfocusedIndicatorColor = colorResource(id = R.color.blue_screen)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = 5,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMoodScreenPreview() {
    AddMoodScreen(navController = rememberNavController())
}