package com.kuba.calendarium.data.model.internal

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kuba.calendarium.R

enum class ContextMenuOption(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @ColorRes val overrideTint: Int? = null
) {
    EDIT(R.string.edit, R.drawable.ic_edit_24),
    DELETE(R.string.delete, R.drawable.ic_delete_24, R.color.color_error)
}