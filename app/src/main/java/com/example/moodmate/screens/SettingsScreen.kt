package com.example.moodmate.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditButton
import com.example.moodmate.components.EditIconButton
import com.example.moodmate.components.EditOutlinedTextField
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.components.ValidationErrorText
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.util.navigateAndClearBackStack
import com.example.moodmate.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actionState.isSuccess) {
        if (actionState.isSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.password_change_success),
                Toast.LENGTH_SHORT
            ).show()
            navigateAndClearBackStack(
                navController = navController,
                destination = MoodMateScreens.ProfileScreen.route,
                popUpToRoute = MoodMateScreens.SettingsScreen.route
            )
        }
    }

    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.update_password_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Column(modifier = modifier.fillMaxWidth()) {
                        EditOutlinedTextField(
                            value = uiState.currentPassword,
                            onValueChange = viewModel::onCurrentPasswordChange,
                            label = stringResource(id = R.string.current_password_label),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                EditIconButton(
                                    icon = if (uiState.isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    onClick = viewModel::toggleCurrentPasswordVisibility,
                                    contentDescription = null
                                )
                            },
                            visualTransformation = if (uiState.isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )
                        uiState.validationErrors.currentPasswordError?.let {
                            ValidationErrorText(error = it)
                        }
                    }

                    Column(modifier = modifier.fillMaxWidth()) {
                        EditOutlinedTextField(
                            value = uiState.newPassword,
                            onValueChange = viewModel::onNewPasswordChange,
                            label = stringResource(id = R.string.new_password_label),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                EditIconButton(
                                    icon = if (uiState.isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    onClick = viewModel::toggleNewPasswordVisibility,
                                    contentDescription = null
                                )
                            },
                            visualTransformation = if (uiState.isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )
                        )
                        uiState.validationErrors.newPasswordError?.let {
                            ValidationErrorText(error = it)
                        }
                    }

                    Column(modifier = modifier.fillMaxWidth()) {
                        EditOutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChange,
                            label = stringResource(id = R.string.confirm_password_label),
                            leadingIcon = Icons.Default.Lock,
                            trailingIcon = {
                                EditIconButton(
                                    icon = if (uiState.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    onClick = viewModel::toggleConfirmPasswordVisibility,
                                    contentDescription = null
                                )
                            },
                            visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                        uiState.validationErrors.confirmPasswordError?.let {
                            ValidationErrorText(error = it)
                        }
                    }
                }
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditButton(
                    text = stringResource(id = R.string.back_button),
                    onClick = {
                        navigateAndClearBackStack(
                            navController = navController,
                            destination = MoodMateScreens.ProfileScreen.route,
                            popUpToRoute = MoodMateScreens.SettingsScreen.route
                        )
                    },
                    containerColor = Color.LightGray
                )

                EditButton(
                    text = stringResource(id = R.string.save_button),
                    onClick = viewModel::changePassword,
                    containerColor = colorResource(id = R.color.acik_mavi)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}