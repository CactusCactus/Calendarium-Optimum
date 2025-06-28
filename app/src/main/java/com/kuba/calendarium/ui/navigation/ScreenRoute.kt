package com.kuba.calendarium.ui.navigation

sealed class ScreenRoute(val route: String) {
    object Calendar : ScreenRoute("calendar_screen")
}