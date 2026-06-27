package com.example.moodmate.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.moodmate.R
import com.example.moodmate.model.MoodItem

@Composable
fun MoodSelector(
    moods: List<MoodItem>,
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
            modifier = modifier.padding(top = 16.dp, bottom = 0.dp)
        ) {
            items(moods.size) { index ->
                val mood = moods[index]
                val isSelected = selectedMoodIndex == index
                val (scale, backgroundColor) = getMoodSelectionAnimation(isSelected)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .scale(scale.value)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor.value)
                        .border(
                            width = if (isSelected) 0.dp else 2.dp,
                            color = if (isSelected) Color.Transparent else colorResource(id = R.color.blue_screen),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onMoodSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = mood.emoji,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = mood.label,
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
                .padding(top = 8.dp, bottom = 0.dp)
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
                    val (scale, color) = getMoodSelectionAnimation(isSelected)

                    Box(
                        modifier = modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(horizontal = 2.dp)
                            .scale(scale.value)
                            .clip(CircleShape)
                            .background(color.value)
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

        EditTextField(
            value = noteText,
            onValueChange = onNoteChanged,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            placeholder = stringResource(id = R.string.note_placeholder),
            singleLine = false,
            minLines = 4,
            maxLines = 4,
            imeAction = ImeAction.Done,
            focusedIndicatorColor = colorResource(id = R.color.blue_screen),
            unfocusedIndicatorColor = colorResource(id = R.color.blue_screen)
        )
    }
}

@Composable
fun DateTimeSection(
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = stringResource(id = R.string.date_time_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditTextField(
                value = date,
                onValueChange = {},
                modifier = modifier
                    .weight(1f)
                    .clickable { onDateClick() },
                placeholder = stringResource(id = R.string.date_placeholder),
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null
                    )
                }
            )

            EditTextField(
                value = time,
                onValueChange = {},
                modifier = modifier
                    .weight(1f)
                    .clickable { onTimeClick() },
                placeholder = stringResource(id = R.string.time_placeholder),
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
fun getMoodSelectionAnimation(isSelected: Boolean): Pair<State<Float>, State<Color>> {
    val scale = animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val color = animateColorAsState(
        targetValue = if (isSelected) colorResource(id = R.color.acik_mavi) else Color.White,
        animationSpec = tween(300),
        label = "color"
    )

    return scale to color
}
