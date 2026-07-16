package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class ExtendedColors(
    val positive: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
    val negative: Color,
    val negativeContainer: Color,
    val onNegativeContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

val LightExtendedColors = ExtendedColors(
    positive = Color(0xFF386A20),
    positiveContainer = Color(0xFFE8F5E9),
    onPositiveContainer = Color(0xFF0F3815),
    negative = Color(0xFFBA1A1A),
    negativeContainer = Color(0xFFFFDAD6),
    onNegativeContainer = Color(0xFF410002),
    warning = Color(0xFF856404),
    warningContainer = Color(0xFFFFF3CD),
    onWarningContainer = Color(0xFF2B2100)
)

val DarkExtendedColors = ExtendedColors(
    positive = Color(0xFF81C784),
    positiveContainer = Color(0xFF0F3815),
    onPositiveContainer = Color(0xFFC8E6C9),
    negative = Color(0xFFFFB4AB),
    negativeContainer = Color(0xFF410002),
    onNegativeContainer = Color(0xFFFFDAD6),
    warning = Color(0xFFFFD54F),
    warningContainer = Color(0xFF403B2B),
    onWarningContainer = Color(0xFFFFF3CD)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA1C9FF),
    onPrimary = Color(0xFF00325A),
    primaryContainer = Color(0xFF00497E),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF2D323A),
    onSecondaryContainer = Color(0xFFE2E2E9),
    background = Color(0xFF101216),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF1B1D22),
    onSurface = Color(0xFFE2E2E9),
    outline = Color(0xFF43474E),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryContainer,
    onPrimaryContainer = CleanOnPrimaryContainer,
    secondary = CleanSecondary,
    onSecondary = CleanOnSecondary,
    secondaryContainer = CleanSecondaryContainer,
    onSecondaryContainer = CleanOnSecondaryContainer,
    background = CleanBackground,
    onBackground = CleanOnBackground,
    surface = CleanSurface,
    onSurface = CleanOnSurface,
    outline = CleanOutline,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve exact custom theme colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
