package com.example.moodmate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moodmate.R
import com.example.moodmate.navigation.MoodMateScreens
import com.example.moodmate.navigation.bottomBarRoutes
import com.example.moodmate.navigation.bottomNavItems
import com.example.moodmate.navigation.navigateAndClearBackStack

@Composable
fun EditScaffold(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomBarRoutes
    val showBackButton = !showBottomBar

    val showTopBar = currentRoute != MoodMateScreens.SplashScreen.route &&
            currentRoute != MoodMateScreens.SignInScreen.route &&
            currentRoute != MoodMateScreens.SignUpScreen.route

    Scaffold(
        topBar = {
            if (showTopBar) {
                EditTopAppBar(
                    title = title,
                    navController = navController,
                    showBackButton = showBackButton
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                EditBottomAppBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background_blue))
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTopAppBar(
    title: String,
    navController: NavController,
    showBackButton: Boolean
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                EditIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    onClick = { navController.popBackStack() }
                )
            }
        }
    )
}

@Composable
private fun EditBottomAppBar(
    navController: NavController,
    currentRoute: String?
) {
    val acikMavi = colorResource(id = R.color.acik_mavi)

    NavigationBar(containerColor = Color.White) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navigateAndClearBackStack(
                        navController = navController,
                        destination = item.route,
                        popUpToRoute = MoodMateScreens.HomeScreen.route,
                        inclusive = item.route == MoodMateScreens.HomeScreen.route
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = stringResource(id = item.labelResId))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = acikMavi,
                    selectedTextColor = acikMavi,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = acikMavi.copy(alpha = 0.1f)
                )
            )
        }
    }
}
