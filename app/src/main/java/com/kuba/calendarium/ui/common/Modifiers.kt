package com.kuba.calendarium.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.textFieldClickable(key: Any, onClick: () -> Unit): Modifier =
    this.pointerInput(key) {
        awaitEachGesture {
            // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
            // in the Initial pass to observe events before the text field consumes them
            // in the Main pass.
            awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) {
                onClick()
            }
        }
    }