package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KernelXColorScheme = darkColorScheme(
  primary = ElectricRed,
  onPrimary = Color.White,
  primaryContainer = ElectricRedDark,
  onPrimaryContainer = Color.White,
  secondary = ElectricRedLight,
  onSecondary = Color.Black,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = TextPrimary,
  tertiary = TerminalCyan,
  onTertiary = Color.Black,
  background = DeepBlack,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceCard,
  onSurfaceVariant = TextSecondary,
  outline = DarkSurfaceBorder,
  outlineVariant = ElectricRedGlow,
  error = TerminalError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = KernelXColorScheme,
    typography = Typography,
    content = content
  )
}
