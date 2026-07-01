package com.example.moodmate.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.example.moodmate.presentation.navigation.MoodMateScreens
import com.example.moodmate.presentation.navigation.bottomBarRoutes
import com.example.moodmate.presentation.navigation.bottomNavItems
import com.example.moodmate.presentation.navigation.navigateAndClearBackStack

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

    NavigationBar(
        containerColor = Color.White,
        contentColor = acikMavi
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            if (index == 2) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        FloatingActionButton(
                            onClick = {
                                navigateAndClearBackStack(
                                    navController = navController,
                                    destination = MoodMateScreens.AddMoodScreen.route,
                                    popUpToRoute = MoodMateScreens.HomeScreen.route,
                                    inclusive = false
                                )
                            },
                            containerColor = acikMavi,
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navigateAndClearBackStack(
                                navController = navController,
                                destination = item.route,
                                popUpToRoute = MoodMateScreens.HomeScreen.route,
                                inclusive = item.route == MoodMateScreens.HomeScreen.route
                            )
                        }
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
}
