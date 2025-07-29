// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/theme/Theme.kt
package com.github.bbqribs.pushupstracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.github.bbqribs.pushupstracker.ui.theme.AppTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ✅ STEP 1: Define the custom colors you want to add to the theme.
data class CustomColors(
    val cardBackground: Color
)

// ✅ STEP 2: Define the actual color values for light and dark themes.
private val DarkCustomColors = CustomColors(
    cardBackground = Color(0xFF2D2D3A) // A dark, pastel purple/slate
)

private val LightCustomColors = CustomColors(
    cardBackground = Color(0xFFF0F0F8) // A very light, pastel purple/slate
)

// ✅ STEP 3: Create a CompositionLocal to provide the custom colors down the hierarchy.
private val LocalCustomColors = staticCompositionLocalOf {
    // Provide a default. This will be replaced by the values in the theme.
    LightCustomColors
}

// ✅ STEP 4: Create a convenient accessor object to easily call your custom colors.
object AppTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}

// --- Your original light and dark color schemes ---
private val LightColors = lightColorScheme(
    primary   = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    background= Color(0xFFFFFBFE)
)

private val DarkColors = darkColorScheme(
    primary   = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    background= Color(0xFF1C1B1F)
)

@Composable
fun PushupsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Renamed for clarity
    content: @Composable () -> Unit
) {
    // ✅ STEP 5: Select the correct color palette (standard and custom) based on the theme.
    val colors = if (darkTheme) DarkColors else LightColors
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    // ✅ STEP 6: Provide both the standard MaterialTheme and your custom colors.
    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colors,
            typography  = AppTypography,
            content     = content
        )
    }
}