package com.example.smartfit.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SmartFitColorScheme = darkColorScheme(
    primary = SmartFitPrimary,
    onPrimary = SmartFitOnPrimary,
    primaryContainer = SmartFitPrimaryContainer,
    onPrimaryContainer = SmartFitOnPrimaryContainer,
    secondary = SmartFitSecondary,
    onSecondary = SmartFitOnSecondary,
    secondaryContainer = SmartFitSecondaryContainer,
    onSecondaryContainer = SmartFitOnSecondaryContainer,
    tertiary = SmartFitTertiary,
    onTertiary = SmartFitOnTertiary,
    tertiaryContainer = SmartFitTertiaryContainer,
    onTertiaryContainer = SmartFitOnTertiaryContainer,
    background = SmartFitBackground,
    onBackground = SmartFitOnBackground,
    surface = SmartFitSurface,
    onSurface = SmartFitOnSurface,
    surfaceVariant = SmartFitSurfaceVariant,
    onSurfaceVariant = SmartFitOnSurfaceVariant,
    error = SmartFitError,
    onError = SmartFitOnError,
    errorContainer = SmartFitErrorContainer,
    onErrorContainer = SmartFitOnErrorContainer,
    outline = SmartFitOutline,
    outlineVariant = SmartFitOutlineVariant,
    scrim = SmartFitScrim,
    inversePrimary = SmartFitInversePrimary,
    inverseSurface = SmartFitInverseSurface,
    inverseOnSurface = SmartFitInverseOnSurface,
    surfaceTint = SmartFitPrimary,
)

@Composable
fun SmartFitTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = SmartFitColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}