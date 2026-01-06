package com.example.moodmate.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ValidationErrorText(
    error: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = error,
        style = MaterialTheme.typography.bodySmall,
        color = Color.Red,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp)
    )
}