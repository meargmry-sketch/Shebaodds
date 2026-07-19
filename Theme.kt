package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = compositionLocalOf { true }

// Modern styled-components / CSS variables theme design token wrapper
data class AppColorScheme(
    val bg: Color,
    val card: Color,
    val surfaceL2: Color,
    val border: Color,
    val textWhite: Color,
    val textLight: Color,
    val textMuted: Color,
    val textGrey: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color
)

// Dynamic theme color configurations
val DarkAppColors = AppColorScheme(
    bg = RawSlateDarkBG,
    card = RawSlateCardBG,
    surfaceL2 = RawSlateSurfaceL2,
    border = RawBorderColor,
    textWhite = RawTextWhite,
    textLight = RawTextLight,
    textMuted = RawTextMuted,
    textGrey = RawTextGrey,
    primary = PrimarySapphire,
    secondary = NeonGreen,
    tertiary = AmberAccent,
    error = LightRed
)

val LightAppColors = AppColorScheme(
    bg = Color(0xFFF1F5F9),
    card = Color(0xFFFFFFFF),
    surfaceL2 = Color(0xFFE2E8F0),
    border = Color(0xFFCBD5E1),
    textWhite = Color(0xFF0F172A),
    textLight = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    textGrey = Color(0xFF94A3B8),
    primary = PrimarySapphire,
    secondary = Color(0xFF059669),
    tertiary = AmberAccent,
    error = Color(0xFFDC2626)
)

val LocalAppColors = staticCompositionLocalOf<AppColorScheme> {
    DarkAppColors // Default to Dark design tokens
}

object AppTheme {
    val colors: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeIsDark.current
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySapphire,
    onPrimary = RawTextWhite,
    secondary = NeonGreen,
    onSecondary = RawSlateDarkBG,
    tertiary = AmberAccent,
    background = RawSlateDarkBG,
    onBackground = RawTextLight,
    surface = RawSlateCardBG,
    onSurface = RawTextWhite,
    outline = RawBorderColor,
    surfaceVariant = RawSlateSurfaceL2,
    onSurfaceVariant = RawTextLight,
    error = LightRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimarySapphire,
    onPrimary = Color.White,
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFCBD5E1),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    error = Color(0xFFDC2626)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalThemeIsDark provides darkTheme,
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
