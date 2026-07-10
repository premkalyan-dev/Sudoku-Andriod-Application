package com.prem.skudo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColorName: String = "Cyan",
    content: @Composable () -> Unit
) {
    val accentColor = when (accentColorName) {
        "Cyan" -> PrimaryCyan
        "Gold" -> AccentGold
        "Green" -> EasyGreen
        "Purple" -> HardPurple
        "Red" -> ExpertRed
        else -> PrimaryCyan
    }

    val secondaryColor = when (accentColorName) {
        "Cyan" -> SecondaryCyan
        "Gold" -> Color(0xFFF39C12)
        "Green" -> Color(0xFF2ECC71)
        "Purple" -> Color(0xFF9B59B6)
        "Red" -> Color(0xFFE74C3C)
        else -> SecondaryCyan
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = secondaryColor,
            secondary = accentColor,
            tertiary = AccentGold,
            background = Color(0xFF000814),
            surface = Color(0xFF001229),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFE9ECEF),
            onSurface = Color(0xFFE9ECEF),
            error = WrongRed
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            secondary = secondaryColor,
            tertiary = AccentGold,
            background = LightBackground,
            surface = LightSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = TextDark,
            onSurface = TextDark,
            error = WrongRed
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
