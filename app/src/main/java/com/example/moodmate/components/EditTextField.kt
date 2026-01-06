package com.example.moodmate.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    focusedContainerColor: Color = Color.White,
    unfocusedContainerColor: Color = Color.White,
    disabledContainerColor: Color = Color.White,
    focusedIndicatorColor: Color = Color.Transparent,
    unfocusedIndicatorColor: Color = Color.Transparent,
    disabledIndicatorColor: Color = Color.Transparent,
    disabledTextColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = focusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            disabledContainerColor = disabledContainerColor,
            focusedIndicatorColor = focusedIndicatorColor,
            unfocusedIndicatorColor = unfocusedIndicatorColor,
            disabledIndicatorColor = disabledIndicatorColor,
            disabledTextColor = disabledTextColor,
            disabledLeadingIconColor = disabledLeadingIconColor,
            disabledPlaceholderColor = disabledPlaceholderColor
        )
    )
}