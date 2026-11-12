package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = DeepNavy,
    primaryContainer = CardDark,
    onPrimaryContainer = TealPrimary,
    secondary = AccentOrange,
    onSecondary = DeepNavy,
    secondaryContainer = AccentOrangeLight,
    tertiary = EmeraldSuccess,
    background = DeepNavy,
    onBackground = TextPrimaryDark,
    surface = DarkNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = TealDark,
    secondary = AccentOrange,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFFFE0B2),
    tertiary = EmeraldSuccess,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = ErrorRed
)

@Composable
fun VidGetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
