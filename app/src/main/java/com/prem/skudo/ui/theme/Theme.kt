package com.prem.skudo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColorName: String = "Cyan",
    content: @Composable () -> Unit
) {
    val accentColor = when (accentColorName) {
        "Cyan" -> PrimaryCyan
        "Gold" -> AccentGold
        else -> PrimaryCyan
    }

    val secondaryColor = when (accentColorName) {
        "Cyan" -> SecondaryCyan
        "Gold" -> Color(0xFFF39C12)
        else -> SecondaryCyan
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = if (accentColorName == "Gold") AccentGold else SecondaryCyan,
            secondary = accentColor,
            tertiary = AccentGold,
            background = Color(0xFF000814),
            surface = Color(0xFF001229),
            surfaceVariant = Color(0xFF0D1B2A),
            outline = Color(0xFF20324D),
            outlineVariant = Color(0xFF1B2A4A),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF94A3B8),
            error = WrongRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            secondary = secondaryColor,
            tertiary = AccentGold,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightCard,
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = TextDark,
            onSurface = TextDark,
            onSurfaceVariant = TextMuted,
            error = WrongRed,
            onError = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
