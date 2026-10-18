package com.coder7475.focusguard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SafetyOrange,
    onPrimary = MatteCharcoal,
    secondary = SignalAmber,
    onSecondary = MatteCharcoal,
    tertiary = ElectricCyan,
    background = MatteCharcoal,
    onBackground = OffWhite,
    surface = DarkGunmetal,
    onSurface = OffWhite,
    surfaceVariant = DarkGunmetal,
    onSurfaceVariant = TextMuted,
    outline = BorderDark,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyOrange,
    onPrimary = OffWhite,
    secondary = SignalAmber,
    onSecondary = MatteCharcoal,
    tertiary = ElectricCyan,
    background = OffWhite,
    onBackground = MatteCharcoal,
    surface = DarkGunmetal,
    onSurface = OffWhite,
    outline = BorderDark,
    error = DangerRed
)

@Composable
fun FocusGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to preserve branded industrial aesthetics
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Force dark industrial theme by default for focus discipline
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}