package com.kuba.calendarium.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute(val route: String) {
    object Calendar : ScreenRoute("calendar_screen")
    object AddEvent : ScreenRoute("add_event_screen")
}