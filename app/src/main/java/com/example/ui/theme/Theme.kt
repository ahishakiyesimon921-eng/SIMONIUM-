package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SimoniumCyanLight,
    onPrimary = SimoniumNavyDark,
    primaryContainer = SimoniumTealContainer,
    onPrimaryContainer = SimoniumCyanLight,
    secondary = SimoniumTealLight,
    onSecondary = SimoniumNavyDark,
    secondaryContainer = SimoniumNavyElevated,
    onSecondaryContainer = Color.White,
    tertiary = SimoniumCyan,
    background = SimoniumNavyDark,
    surface = SimoniumNavySurface,
    onBackground = SimoniumTextPrimaryDark,
    onSurface = SimoniumTextPrimaryDark,
    surfaceVariant = SimoniumNavyElevated,
    onSurfaceVariant = SimoniumTextSecondaryDark,
    outline = Color(0xFF233554)
)

private val LightColorScheme = lightColorScheme(
    primary = SimoniumTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = SimoniumNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = SimoniumNavy,
    tertiary = SimoniumCyan,
    background = SimoniumBackgroundLight,
    surface = SimoniumSurfaceLight,
    onBackground = SimoniumTextPrimaryLight,
    onSurface = SimoniumTextPrimaryLight,
    surfaceVariant = SimoniumSurfaceVariantLight,
    onSurfaceVariant = SimoniumTextSecondaryLight,
    outline = SimoniumOutlineLight
)

@Composable
fun SimoniumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Navy & Teal identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
