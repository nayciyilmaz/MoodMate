package com.example.moodmate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditButton
import com.example.moodmate.components.EditOutlinedTextField
import com.example.moodmate.components.EditTextButton
import com.example.moodmate.components.ValidationErrorText
import com.example.moodmate.navigation.MoodMateScreens
import androidx.compose.foundation.text.KeyboardOptions
import com.example.moodmate.components.EditIconButton
import com.example.moodmate.util.navigateAndClearBackStack
import com.example.moodmate.viewmodel.SignInViewModel

@Composable
fun SignInScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState.isSuccess) {
        if (actionState.isSuccess) {
            navigateAndClearBackStack(
                navController = navController,
                destination = MoodMateScreens.HomeScreen.route,
                popUpToRoute = MoodMateScreens.SignInScreen.route
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.hos_geldiniz_baslik),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(id = R.string.giris_alt_baslik),
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(bottom = 4.dp)
        )

        Column(modifier = modifier.fillMaxWidth()) {
            EditOutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(id = R.string.eposta_etiket),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            uiState.validationErrors.emailError?.let {
                ValidationErrorText(error = it)
            }
        }

        Column(modifier = modifier.fillMaxWidth()) {
            EditOutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(id = R.string.sifre_etiket),
                leadingIcon = Icons.Default.Lock,
                trailingIcon = {
                    EditIconButton(
                        icon = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        onClick = viewModel::togglePasswordVisibility,
                        contentDescription = null
                    )
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            uiState.validationErrors.passwordError?.let {
                ValidationErrorText(error = it)
            }
        }

        EditButton(
            text = stringResource(id = R.string.giris_yap_butonu),
            onClick = { viewModel.login() },
            containerColor = colorResource(id = R.color.acik_mavi),
            modifier = modifier.padding(top = 8.dp)
        )

        EditTextButton(
            text = stringResource(id = R.string.hesap_olustur_butonu),
            onClick = { navController.navigate(MoodMateScreens.SignUpScreen.route) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(navController = rememberNavController())
}