package com.kuba.calendarium.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenRoute(val route: String) {
    object Calendar : ScreenRoute("calendar_screen")
}