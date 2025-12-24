package com.example.moodmate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditButton
import com.example.moodmate.components.EditOutlinedTextField
import com.example.moodmate.components.EditTextButton
import com.example.moodmate.navigation.MoodMateScreens
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun SignUpScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.hesap_olustur_butonu),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.acik_mavi)
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditOutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = stringResource(id = R.string.isim_etiket),
                leadingIcon = Icons.Default.Person,
                modifier = modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            EditOutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = stringResource(id = R.string.soyisim_etiket),
                leadingIcon = Icons.Default.Person,
                modifier = modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
        }

        EditOutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(id = R.string.eposta_etiket),
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        EditOutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(id = R.string.sifre_etiket),
            leadingIcon = Icons.Default.Lock,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = modifier.padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        EditButton(
            text = stringResource(id = R.string.hesap_olustur_butonu),
            onClick = { },
            containerColor = colorResource(id = R.color.acik_mavi)
        )

        EditTextButton(
            text = stringResource(id = R.string.giris_yap_butonu),
            onClick = { navController.navigate(MoodMateScreens.SignInScreen.route) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(navController = rememberNavController())
}