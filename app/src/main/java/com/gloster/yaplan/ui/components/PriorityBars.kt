package com.gloster.yaplan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gloster.yaplan.data.Priority
import com.gloster.yaplan.ui.theme.Yap

/** Text color for a priority label; only "Yüksek" stands out. */
fun Priority.labelColor(dimmed: Boolean = false): Color = when {
    dimmed -> Yap.TextFaint
    this == Priority.HIGH -> Yap.Danger
    else -> Yap.TextMuted
}

/** Three signal bars: high fills all three in red, medium two, low one. */
@Composable
fun PriorityBars(priority: Priority, modifier: Modifier = Modifier, dimmed: Boolean = false) {
    val off = Yap.Outline
    val fills = when {
        dimmed -> listOf(off, off, off)
        priority == Priority.HIGH -> List(3) { Yap.Danger }
        priority == Priority.MEDIUM -> listOf(Yap.TextSecondary, Yap.TextSecondary, off)
        else -> listOf(Yap.TextFaint, off, off)
    }
    Canvas(modifier.size(width = 13.dp, height = 12.dp)) {
        val barWidth = size.width * 3 / 13
        val step = size.width * 5 / 13
        val heights = listOf(5f / 12, 8f / 12, 1f)
        fills.forEachIndexed { i, color ->
            val h = size.height * heights[i]
            drawRect(color, topLeft = Offset(i * step, size.height - h), size = Size(barWidth, h))
        }
    }
}
