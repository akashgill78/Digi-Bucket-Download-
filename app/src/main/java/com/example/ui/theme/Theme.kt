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

private val ProfessionalPolishLightColorScheme =
  lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandOnPrimaryContainer,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = BrandOnSecondaryContainer,
    tertiary = BrandTertiary,
    onTertiary = Color.White,
    tertiaryContainer = BrandTertiaryContainer,
    onTertiaryContainer = BrandOnTertiaryContainer,
    background = PolishBackground,
    onBackground = TextPrimary,
    surface = PolishSurface,
    onSurface = TextPrimary,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = PolishBorder,
    outlineVariant = PolishBorderSubtle
  )

private val ProfessionalPolishDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = PolishDarkBg,
    onBackground = Color(0xFFE6E1E5),
    surface = PolishDarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = PolishDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = PolishDarkBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to clean Professional Polish theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ProfessionalPolishDarkColorScheme else ProfessionalPolishLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
