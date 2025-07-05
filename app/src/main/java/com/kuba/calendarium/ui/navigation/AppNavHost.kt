package com.kuba.calendarium.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kuba.calendarium.ui.screens.calendar.CalendarScreen
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel
import com.kuba.calendarium.ui.screens.event.addEvent.AddEventScreen
import com.kuba.calendarium.ui.screens.event.editEvent.EditEvenScreen
import com.kuba.calendarium.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Calendar.route
    ) {
        composable(route = ScreenRoute.Calendar.route) { backStackEntry ->
            val viewModel = hiltViewModel<CalendarViewModel>()

            val newEventDateMillis =
                backStackEntry.savedStateHandle.get<Long>(KEY_RESULT_EVENT_DATE_MS)

            LaunchedEffect(newEventDateMillis) {
                if (newEventDateMillis != null) {
                    viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(newEventDateMillis))
                    backStackEntry.savedStateHandle.remove<Long>(KEY_RESULT_EVENT_DATE_MS)
                }
            }

            CalendarScreen(
                viewModel = viewModel,
                onNavigateToAddEvent = {
                    navController.navigate(ScreenRoute.AddEvent.createRoute(it))
                },
                onNavigateToEditEvent = {
                    navController.navigate(ScreenRoute.EditEvent.createRoute(it))
                },
                onNavigateToSettings = {
                    navController.navigate(ScreenRoute.Settings.route)
                })

        }

        composable(
            route = ScreenRoute.AddEvent.route,
            arguments = listOf(navArgument(ARG_SELECTED_DATE_MS) { type = NavType.LongType })
        ) {
            AddEventScreen(
                viewModel = hiltViewModel(),
                onNavigateUp = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        KEY_RESULT_EVENT_DATE_MS,
                        it
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = ScreenRoute.EditEvent.route,
            arguments = listOf(navArgument(ARG_EVENT_ID) { type = NavType.LongType })
        ) {
            EditEvenScreen(
                viewModel = hiltViewModel(),
                onNavigateUp = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        KEY_RESULT_EVENT_DATE_MS,
                        it
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(route = ScreenRoute.Settings.route) {
            SettingsScreen(viewModel = hiltViewModel())
        }
    }
}