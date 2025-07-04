package com.kuba.calendarium.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

@Composable
fun StandardListRow(
    label: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    @ColorRes iconTint: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(standardPadding)
    ) {
        val tint = iconTint?.let { colorResource(it) } ?: LocalContentColor.current

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