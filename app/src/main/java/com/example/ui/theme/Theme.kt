package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pristine White Theme: Clean, luminous white surfaces with sharp navy and crimson accents
private val WhiteThemeColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = PureWhite,
    primaryContainer = Navy50,
    onPrimaryContainer = Navy900,
    secondary = PeruRed,
    onSecondary = PureWhite,
    secondaryContainer = PeruRedLight,
    onSecondaryContainer = Navy900,
    tertiary = Gold600,
    onTertiary = PureWhite,
    tertiaryContainer = Gold100,
    onTertiaryContainer = Slate900,
    background = PorcelainWhite,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    outlineVariant = Slate100,
    error = PeruRed,
    errorContainer = PeruRedLight,
    onError = PureWhite
)

@Composable
fun English4EveryoneTheme(
    darkTheme: Boolean = false, // Always enforce the requested pristine white theme
    content: @Composable () -> Unit
) {
    val colorScheme = WhiteThemeColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Set system status bar and navigation bar to pure white with dark icons
                window.statusBarColor = PureWhite.toArgb()
                window.navigationBarColor = PureWhite.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
