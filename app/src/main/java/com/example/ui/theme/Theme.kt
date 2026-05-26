package com.example.ui.theme

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
    primary = AgriPrimaryDark,
    secondary = AgriSecondaryDark,
    tertiary = AgriTertiaryDark,
    background = AgriBackgroundDark,
    surface = AgriSurfaceDark,
    surfaceVariant = AgriSurfaceVariantDark,
    onPrimary = AgriOnPrimaryDark,
    onSecondary = AgriOnSecondaryDark,
    onBackground = AgriOnBackgroundDark,
    onSurface = AgriOnSurfaceDark,
    error = ErrorColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = AgriPrimary,
    secondary = AgriSecondary,
    tertiary = AgriTertiary,
    background = AgriBackground,
    surface = AgriSurface,
    surfaceVariant = AgriSurfaceVariant,
    onPrimary = AgriOnPrimary,
    onSecondary = AgriOnSecondary,
    onBackground = AgriOnBackground,
    onSurface = AgriOnSurface,
    error = ErrorColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow turning off dynamic system colors to preserve our distinctive custom agri theme branding
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
