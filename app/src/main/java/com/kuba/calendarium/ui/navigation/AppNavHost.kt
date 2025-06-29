package com.kuba.calendarium.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onNavigateToAddEvent = {
                    navController.navigate(ScreenRoute.AddEvent.createRoute(it))
                })
        }

        composable(
            route = ScreenRoute.AddEvent.route,
            arguments = listOf(navArgument(ARG_SELECTED_DATE_MS) { type = NavType.LongType })
        ) {
            AddEventScreen(
                viewModel = hiltViewModel()
            )
        }
    }
}