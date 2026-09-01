package com.thewalkersoft.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Rose80,
    onPrimary = Rose20,
    primaryContainer = Rose20,
    onPrimaryContainer = Rose80,
    secondary = Berry80,
    onSecondary = Berry20,
    secondaryContainer = Berry20,
    onSecondaryContainer = Berry80,
    tertiary = Lavender80,
    onTertiary = Lavender20,
    background = SurfaceDark,
    onBackground = Color(0xFFF3EDEE),
    surface = CardBackgroundDark,
    onSurface = Color(0xFFF3EDEE),
    surfaceVariant = Color(0xFF332A2D),
    onSurfaceVariant = Color(0xFFD6C2C7),
    outline = Color(0xFF4A3E42),
    outlineVariant = Color(0xFF362C30)
)

private val LightColorScheme = lightColorScheme(
    primary = Rose40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECEF),
    onPrimaryContainer = Rose20,
    secondary = Berry40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDE8F3),
    onSecondaryContainer = Berry20,
    tertiary = Lavender40,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF22191B),
    surface = CardBackgroundLight,
    onSurface = Color(0xFF22191B),
    surfaceVariant = Color(0xFFFBF0F3),
    onSurfaceVariant = Color(0xFF75656A),
    outline = Color(0xFFE8D7DC),
    outlineVariant = Color(0xFFF3E7EA)
)

@Composable
fun CycleTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted palette by default for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // We don't set statusBarColor manually here because enableEdgeToEdge() is used in MainActivity.
                // Setting it here can cause infinite measurement loops in DecorView on some Android versions.
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
