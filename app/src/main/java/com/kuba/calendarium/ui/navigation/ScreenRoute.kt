package com.kuba.calendarium.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute(val route: String) {
    object Calendar : ScreenRoute("calendar_screen")
    object AddEvent : ScreenRoute("add_event_screen/{$ARG_SELECTED_DATE_MS}") {
        fun createRoute(selectedDate: Long) = "add_event_screen/$selectedDate"
    }
}

const val ARG_SELECTED_DATE_MS = "selectedDate"
