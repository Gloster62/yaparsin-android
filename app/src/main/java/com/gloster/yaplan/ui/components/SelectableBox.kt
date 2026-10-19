package com.gloster.yaplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.theme.Yap

/**
 * The one "selected = yellow outline on a dark-yellow tint" control used for tabs, chips,
 * priority, date and time choices.
 */
@Composable
fun SelectableBox(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton,
    height: Dp = 48.dp,
    corner: Dp = 6.dp,
    unselectedBorder: Color = Yap.Outline,
    horizontalPadding: Dp = 12.dp,
    interactive: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Row(
        modifier
            .height(height)
            .clip(shape)
            .background(if (selected) Yap.AccentContainer else Color.Transparent)
            .border(1.dp, if (selected) Yap.Accent else unselectedBorder, shape)
            .then(if (interactive) Modifier.selectable(selected = selected, onClick = onClick, role = role) else Modifier)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (selected) Yap.Accent else Yap.TextSecondary,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
        ) {
            content()
        }
    }
}

/** A 36dp chip inside a 48dp touch target. */
@Composable
fun ChoiceChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .selectable(selected = selected, onClick = onClick, role = role),
        contentAlignment = Alignment.Center,
    ) {
        SelectableBox(
            selected = selected,
            onClick = onClick,
            height = 36.dp,
            corner = 4.dp,
            interactive = false,
            content = content,
        )
    }
}
