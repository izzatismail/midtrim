package com.izzatismail.midtrim.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AccentPrimaryPressed,
    secondary = PremiumAccent,
    background = BgPrimary,
    surface = BgElevated,
    surfaceVariant = BgSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    error = ColorError,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = AccentPrimaryPressed,
    secondary = PremiumAccentLight,
    background = BgPrimaryLight,
    surface = BgElevatedLight,
    surfaceVariant = BgSurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    error = ErrorColorLight,
    onError = TextPrimary
)

@Composable
fun MidTrimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MidTrimTypography,
        content = content
    )
}