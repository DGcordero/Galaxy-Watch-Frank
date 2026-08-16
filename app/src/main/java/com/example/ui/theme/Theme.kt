package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GalaxyCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5A),
    onPrimaryContainer = Color(0xFF98F0FF),
    secondary = GalaxyUltraOrange,
    onSecondary = Color(0xFF492000),
    secondaryContainer = Color(0xFF6B3100),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = GalaxyEmerald,
    onTertiary = Color(0xFF003918),
    tertiaryContainer = Color(0xFF005325),
    onTertiaryContainer = Color(0xFF6CFFA0),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF334155)
)

private val LightColorScheme = darkColorScheme( // Keep high-contrast modern dark style as default for Galaxy Watch theme
    primary = GalaxyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = GalaxyUltraOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF2C1000),
    tertiary = GalaxyEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun GalaxyWatchStudioTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Force custom polished watch studio colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
