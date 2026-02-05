package com.example.moodmate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.colorResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moodmate.R
import com.example.moodmate.components.EditButton
import com.example.moodmate.components.EditScaffold
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.util.navigateAndClearBackStack
import com.example.moodmate.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    fullName: String,
    email: String,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val shouldNavigateToLogin by viewModel.shouldNavigateToLogin.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin) {
            navigateAndClearBackStack(
                navController = navController,
                destination = MoodMateScreens.SignInScreen.route,
                popUpToRoute = MoodMateScreens.ProfileScreen.route
            )
            viewModel.resetNavigationFlag()
        }
    }

    EditScaffold(navController = navController) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserInfoCard(
                title = stringResource(id = R.string.user_information),
                userName = fullName,
                userEmail = email
            )

            SettingCard(
                title = stringResource(id = R.string.theme_selection),
                options = listOf(
                    stringResource(id = R.string.light_theme),
                    stringResource(id = R.string.dark_theme)
                ),
                selectedOption = selectedTheme,
                onOptionSelected = viewModel::setTheme
            )

            SettingCard(
                title = stringResource(id = R.string.notification_permission),
                options = listOf(
                    stringResource(id = R.string.on),
                    stringResource(id = R.string.off)
                ),
                selectedOption = notificationEnabled,
                onOptionSelected = viewModel::setNotification
            )

            SettingCard(
                title = stringResource(id = R.string.language_selection),
                options = listOf(
                    stringResource(id = R.string.turkish),
                    stringResource(id = R.string.english),
                    stringResource(id = R.string.spanish),
                    stringResource(id = R.string.italian)
                ),
                selectedOption = selectedLanguage,
                onOptionSelected = viewModel::setLanguage
            )

            EditButton(
                text = stringResource(id = R.string.logout),
                onClick = { viewModel.logout() },
                containerColor = Color.Red
            )
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            options.forEach { option ->
                OptionItem(
                    text = option,
                    isSelected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Composable
fun OptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) colorResource(id = R.color.acik_mavi)
                else colorResource(id = R.color.light_gray_background)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun UserInfoCard(
    title: String,
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = stringResource(id = R.string.username),
                value = userName,
                modifier = Modifier.padding(top = 12.dp)
            )

            HorizontalDivider(
                modifier = modifier.padding(vertical = 16.dp)
            )

            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = stringResource(id = R.string.eposta_etiket),
                value = userEmail
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        navController = rememberNavController(),
        fullName = "Yılmaz Naycı",
        email = "yilmaznayci@gmail.com"
    )
}