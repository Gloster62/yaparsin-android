package com.gloster.yaplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.theme.Yap

enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    TASKS("tasks", "Görevler", Icons.Outlined.Checklist),
    HABITS("habits", "Alışkanlıklar", Icons.Outlined.Link),
    SEARCH("search", "Ara", Icons.Outlined.Search),
    CATEGORIES("categories", "Kategoriler", Icons.Outlined.Sell),
}

/** 64dp bar with labelled icons; Material's NavigationBar is fixed at 80dp. */
@Composable
fun AppBottomBar(current: TopLevelDestination?, onSelect: (TopLevelDestination) -> Unit) {
    Column(Modifier.background(Yap.Surface)) {
        HorizontalDivider(color = Yap.Border)
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .selectableGroup(),
        ) {
            TopLevelDestination.entries.forEach { dest ->
                val selected = dest == current
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(dest) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Box(
                        Modifier
                            .size(width = 56.dp, height = 28.dp)
                            .background(if (selected) Yap.AccentContainer else Color.Transparent, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            dest.icon,
                            contentDescription = null,
                            tint = if (selected) Yap.Accent else Yap.TextMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        dest.label,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Yap.Text else Yap.TextMuted,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
