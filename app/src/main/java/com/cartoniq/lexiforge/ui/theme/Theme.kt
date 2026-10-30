package com.cartoniq.lexiforge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MidnightScholarColors = darkColorScheme(
    primary = Emerald,
    onPrimary = MidnightBackground,
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = OffWhite,
    secondary = Amber,
    onSecondary = MidnightBackground,
    tertiary = EasyBlue,
    onTertiary = Color.White,
    background = MidnightBackground,
    onBackground = OffWhite,
    surface = MidnightSurface,
    onSurface = OffWhite,
    surfaceVariant = MidnightSurfaceHigh,
    onSurfaceVariant = MutedText,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun LexiForgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MidnightScholarColors,
        typography = LexiTypography,
        content = content
    )
}
