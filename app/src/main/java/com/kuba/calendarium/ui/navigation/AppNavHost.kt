package com.kuba.calendarium.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import java.time.LocalDate

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Calendar.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = ScreenRoute.Calendar.route) { backStackEntry ->
            val viewModel = hiltViewModel<CalendarViewModel>()

            val newEventDate =
                backStackEntry.savedStateHandle.get<LocalDate>(KEY_RESULT_EVENT_DATE_MS)

            LaunchedEffect(newEventDate) {
                if (newEventDate != null) {
                    viewModel.onEvent(CalendarViewModel.UIEvent.DateSelected(newEventDate))
                    backStackEntry.savedStateHandle.remove<LocalDate>(KEY_RESULT_EVENT_DATE_MS)
                }
            }

            CalendarScreen(
                viewModel = viewModel,
                onNavigateToAddEvent = {
                    navController.navigate(ScreenRoute.AddEvent.createRoute(it.toEpochDay()))
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
            arguments = listOf(navArgument(ARG_SELECTED_DATE_EPOCH_DAY) {
                type = NavType.LongType
            }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
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
            arguments = listOf(navArgument(ARG_EVENT_ID) { type = NavType.LongType }),
            enterTransition = { slideInVertically(initialOffsetY = { it }) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }) }
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

        composable(
            route = ScreenRoute.Settings.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }) {
            SettingsScreen(viewModel = hiltViewModel())
        }
    }
}