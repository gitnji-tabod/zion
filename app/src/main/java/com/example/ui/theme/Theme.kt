package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = AmberAccent,
  onPrimary = NavyDark,
  primaryContainer = NavyPrimary,
  onPrimaryContainer = Color.White,
  secondary = GoldSecondary,
  onSecondary = Color.Black,
  secondaryContainer = SurfaceVariantDark,
  onSecondaryContainer = Color.White,
  tertiary = EmeraldSuccess,
  onTertiary = Color.White,
  background = BackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = OutlineDark,
  error = CrimsonError,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE2E8F0),
  onPrimaryContainer = NavyDark,
  secondary = AmberAccent,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFFFEF3C7),
  onSecondaryContainer = Color(0xFF78350F),
  tertiary = EmeraldSuccess,
  onTertiary = Color.White,
  background = BackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = OutlineLight,
  error = CrimsonError,
  onError = Color.White
)

@Composable
fun DriveSchoolTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
