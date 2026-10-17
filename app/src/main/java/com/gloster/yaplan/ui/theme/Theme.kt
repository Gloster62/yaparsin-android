package com.gloster.yaplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gloster.yaplan.data.Priority

private val colors = darkColorScheme(
    primary = Color(0xFFFF6B35),
    onPrimary = Color(0xFF1A0A00),
    primaryContainer = Color(0xFF5C2410),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFE7BDAE),
    secondaryContainer = Color(0xFF3A2A24),
    onSecondaryContainer = Color(0xFFFFDBCC),
    background = Color(0xFF121214),
    onBackground = Color(0xFFE6E1E3),
    surface = Color(0xFF121214),
    onSurface = Color(0xFFE6E1E3),
    surfaceVariant = Color(0xFF2A2A2E),
    onSurfaceVariant = Color(0xFFB8B4B8),
    surfaceContainer = Color(0xFF1C1C1F),
    surfaceContainerHigh = Color(0xFF242428),
    surfaceContainerHighest = Color(0xFF2E2E33),
    outline = Color(0xFF5A5A60),
    error = Color(0xFFFF6E6E),
    inverseSurface = Color(0xFFE6E1E3),
    inverseOnSurface = Color(0xFF1C1C1F),
    inversePrimary = Color(0xFFB33D0E),
)

val Priority.color: Color
    get() = when (this) {
        Priority.HIGH -> Color(0xFFFF5252)
        Priority.MEDIUM -> Color(0xFFFFC107)
        Priority.LOW -> Color(0xFF66BB6A)
    }

@Composable
fun YapLanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
