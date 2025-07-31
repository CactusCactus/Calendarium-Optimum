package com.kuba.calendarium.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun StandardListRow(
    label: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    iconTint: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val tint = iconTint ?: LocalContentColor.current

        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "$label icon",
                tint = tint
            )
        }
        StandardSpacer()

        Text(text = label, maxLines = 1)
    }
}
