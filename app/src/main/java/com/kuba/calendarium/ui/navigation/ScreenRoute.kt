package com.kuba.calendarium.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute(val route: String) {
    object Calendar : ScreenRoute("calendar_screen")
    object AddEvent : ScreenRoute("add_event_screen/{$ARG_SELECTED_DATE_MS}") {
        fun createRoute(selectedDate: Long) = "add_event_screen/$selectedDate"
    }

    object EditEvent : ScreenRoute("edit_event_screen/{$ARG_EVENT_ID_MS}") {
        fun createRoute(selectedDate: Long) = "edit_event_screen/$selectedDate"
    }
}

const val ARG_SELECTED_DATE_MS = "selectedDate"

const val ARG_EVENT_ID_MS = "selectedDate"

const val KEY_RESULT_EVENT_DATE_MS = "EVENT_DATE_MS"
