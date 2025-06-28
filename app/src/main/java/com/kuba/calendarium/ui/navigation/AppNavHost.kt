package com.kuba.calendarium.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuba.calendarium.ui.screens.CalendarScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Calendar.route
    ) {
        composable(route = ScreenRoute.Calendar.route) {
            CalendarScreen()
        }
    }
}