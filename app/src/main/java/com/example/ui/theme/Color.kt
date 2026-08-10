package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

// Raw BetMaster Premium Design Tokens
internal val RawSlateDarkBG = Color(0xFF0E1321)      // Elegant slate navy background from the picture
internal val RawSlateCardBG = Color(0xFF151F32)      // Lighter blueprint card surface
internal val RawSlateSurfaceL2 = Color(0xFF1E2941)   // Multi-layer panel surface overlay
internal val RawBorderColor = Color(0xFF1D263B)      // Sleek grid lines and border frames

val PrimarySapphire = Color(0xFF2563EB)   // Beautiful Royal / Neon Blue accent button
val NeonGreen = Color(0xFF10B981)        // Sage active success, won status elements
val LightRed = Color(0xFFEF4444)         // Crimson error indicator, loss alerts, cancel tags
val AmberAccent = Color(0xFFF5A623)      // Professional warning gold and premium sponsor status

internal val RawTextWhite = Color(0xFFFFFFFF)
internal val RawTextLight = Color(0xFFE2E8F0)        // High-readability slate light white
internal val RawTextMuted = Color(0xFF94A3B8)        // Clinical helper slate light grey
internal val RawTextGrey = Color(0xFF64748B)         // Secondary hint text labels

// Dynamic Theme Adapters
val SlateDarkBG: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.bg

val SlateCardBG: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.card

val SlateSurfaceL2: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.surfaceL2

val BorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.border

val TextWhite: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textWhite

val TextLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textLight

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textMuted

val TextGrey: Color
    @Composable
    @ReadOnlyComposable
    get() = AppTheme.colors.textGrey


