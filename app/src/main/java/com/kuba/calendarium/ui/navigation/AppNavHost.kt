package com.kuba.calendarium.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kuba.calendarium.ui.screens.addEvent.AddEventScreen
import com.kuba.calendarium.ui.screens.calendar.CalendarScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Calendar.route
    ) {
        composable(route = ScreenRoute.Calendar.route) {
            CalendarScreen(
                viewModel = hiltViewModel(),
                onNavigateToAddEvent = { navController.navigate(ScreenRoute.AddEvent.route) })
        }

        composable(route = ScreenRoute.AddEvent.route) {
            AddEventScreen(viewModel = hiltViewModel())
        }
    }
}