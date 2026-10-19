package com.gloster.yaplan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.R

object Yap {
    val Background = Color(0xFF0E1012)
    val Surface = Color(0xFF15181B)
    val SheetSurface = Color(0xFF1B1F23)
    val DialogSurface = Color(0xFF1F2327)
    val Divider = Color(0xFF23282D)
    val Border = Color(0xFF2A3036)
    val Outline = Color(0xFF3A424A)
    val OutlineStrong = Color(0xFF4A535C)
    val Text = Color(0xFFE9ECEF)
    val TextSecondary = Color(0xFFC9CFD5)
    val TextMuted = Color(0xFFAEB5BC)
    val TextFaint = Color(0xFF8B939B)
    val CheckboxBorder = Color(0xFF7D868E)
    val EmptyIcon = Color(0xFF6B747C)
    val Accent = Color(0xFFFFD60A)
    val AccentContainer = Color(0xFF24220F)
    val OnAccent = Color(0xFF141414)
    val Danger = Color(0xFFFF6B5A)
    val DangerBorder = Color(0xFF5A2A24)
    val OverdueRow = Color(0xFF1C1514)
    val DeleteZone = Color(0xFF3B1D1A)
    val Highlight = Color(0xFF4A4210)
    val HighlightText = Color(0xFFFFE45C)
}

@OptIn(ExperimentalTextApi::class)
private fun plexSans(weight: FontWeight) = Font(
    R.font.ibm_plex_sans,
    weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val PlexSans = FontFamily(
    plexSans(FontWeight.Normal),
    plexSans(FontWeight.Medium),
    plexSans(FontWeight.SemiBold),
    plexSans(FontWeight.Bold),
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
)

private fun TextStyle.plex() = copy(fontFamily = PlexSans, letterSpacing = 0.sp)

private val typography = Typography().run {
    copy(
        displayLarge = displayLarge.plex(),
        displayMedium = displayMedium.plex(),
        displaySmall = displaySmall.plex(),
        headlineLarge = headlineLarge.plex(),
        headlineMedium = headlineMedium.plex(),
        headlineSmall = headlineSmall.plex(),
        titleLarge = titleLarge.plex(),
        titleMedium = titleMedium.plex(),
        titleSmall = titleSmall.plex(),
        bodyLarge = bodyLarge.plex(),
        bodyMedium = bodyMedium.plex(),
        bodySmall = bodySmall.plex(),
        labelLarge = labelLarge.plex(),
        labelMedium = labelMedium.plex(),
        labelSmall = labelSmall.plex(),
    )
}

private val colors = darkColorScheme(
    primary = Yap.Accent,
    onPrimary = Yap.OnAccent,
    primaryContainer = Yap.AccentContainer,
    onPrimaryContainer = Yap.Accent,
    inversePrimary = Yap.Accent,
    secondary = Yap.TextSecondary,
    onSecondary = Yap.OnAccent,
    secondaryContainer = Yap.AccentContainer,
    onSecondaryContainer = Yap.Accent,
    tertiary = Yap.Accent,
    onTertiary = Yap.OnAccent,
    background = Yap.Background,
    onBackground = Yap.Text,
    surface = Yap.Background,
    onSurface = Yap.Text,
    surfaceVariant = Yap.Border,
    onSurfaceVariant = Yap.TextMuted,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = Yap.Background,
    surfaceContainerLow = Yap.Surface,
    surfaceContainer = Yap.Surface,
    surfaceContainerHigh = Yap.DialogSurface,
    surfaceContainerHighest = Yap.Outline,
    surfaceBright = Yap.Outline,
    surfaceDim = Yap.Background,
    outline = Yap.OutlineStrong,
    outlineVariant = Yap.Divider,
    error = Yap.Danger,
    onError = Yap.OnAccent,
    inverseSurface = Yap.Text,
    inverseOnSurface = Yap.OnAccent,
    scrim = Color(0xFF050607),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp),
)

@Composable
fun YapLanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes, content = content)
}
