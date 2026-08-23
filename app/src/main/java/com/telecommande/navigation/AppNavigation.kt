package com.telecommande.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.telecommande.ui.home.HomeScreen
import com.telecommande.ui.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }
    }
}