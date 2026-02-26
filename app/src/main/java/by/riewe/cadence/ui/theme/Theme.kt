// by/riewe/cadence/ui/theme/Theme.kt
package by.riewe.cadence.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnPrimary,

    tertiary = Tertiary,
    onTertiary = OnPrimary,
    tertiaryContainer = TertiaryLight,
    onTertiaryContainer = OnBackground,

    background = BackgroundLight,
    onBackground = OnBackground,

    surface = SurfaceLight,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF616161),

    error = StatusError,
    onError = OnPrimary,

    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,

    secondary = SecondaryLight,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnPrimary,

    tertiary = TertiaryLight,
    onTertiary = OnBackground,
    tertiaryContainer = Tertiary,
    onTertiaryContainer = OnPrimary,

    background = BackgroundDark,
    onBackground = Color(0xFFE0E0E0),

    surface = SurfaceDark,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = StatusError,
    onError = OnPrimary,

    outline = Color(0xFF757575)
)

@Composable
fun CadenceTheme(
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