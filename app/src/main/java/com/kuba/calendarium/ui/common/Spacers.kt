package com.kuba.calendarium.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StandardSpacer() = Spacer(modifier = Modifier.size(standardPadding))

@Composable
fun StandardDoubleSpacer() = Spacer(modifier = Modifier.size(standardDoublePadding))

@Composable
fun StandardQuadrupleSpacer() = Spacer(modifier = Modifier.size(standardQuadruplePadding))

@Composable
fun StandardHalfSpacer() = Spacer(modifier = Modifier.size(standardHalfPadding))

@Composable
fun StandardQuarterSpacer() = Spacer(modifier = Modifier.size(standardQuarterPadding))
