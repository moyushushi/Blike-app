package com.example.blike.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BlikePink,
    secondary = BlikeBlue,
    background = BlikeBackground
)

@Composable
fun BlikeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = BlikeTypography,
        content = content
    )
}