package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = DriveeGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF024220),
    onPrimaryContainer = DriveeGreenLight,
    secondary = DriveeOrange,
    onSecondary = Color.Black,
    background = DriveeNavy,
    onBackground = DriveeTextLight,
    surface = DriveeSurfaceDark,
    onSurface = DriveeTextLight,
    surfaceVariant = DriveeSurfaceCardDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = DriveeRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DriveeGreenDark,
    onPrimary = Color.White,
    primaryContainer = DriveeGreenContainer,
    onPrimaryContainer = Color(0xFF00381B),
    secondary = DriveeOrange,
    onSecondary = Color.White,
    background = DriveeGrayBg,
    onBackground = DriveeTextDark,
    surface = DriveeGrayCard,
    onSurface = DriveeTextDark,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = DriveeGrayText,
    error = DriveeRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

