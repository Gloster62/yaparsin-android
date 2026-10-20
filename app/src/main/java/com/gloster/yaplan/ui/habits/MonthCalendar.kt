package com.gloster.yaplan.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.data.ChainPart
import com.gloster.yaplan.data.DayStatus
import com.gloster.yaplan.data.canToggle
import com.gloster.yaplan.data.chainPart
import com.gloster.yaplan.data.dayStatus
import com.gloster.yaplan.data.monthWeeks
import com.gloster.yaplan.ui.dayLabel
import com.gloster.yaplan.ui.formatMonth
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.weekdayInitials
import java.time.LocalDate
import java.time.YearMonth

private val cellCorner = 6.dp

/** Done days in a row merge into one yellow band: the chain you shouldn't break. */
@Composable
fun MonthCalendar(
    month: YearMonth,
    today: LocalDate,
    done: Set<LocalDate>,
    startDay: LocalDate,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggle: (LocalDate, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier
            .fillMaxWidth()
            .background(Yap.Surface, shape)
            .border(1.dp, Yap.Border, shape)
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
    ) {
        val arrowColors = IconButtonDefaults.iconButtonColors(contentColor = Yap.Text, disabledContentColor = Yap.Outline)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, enabled = canGoBack, colors = arrowColors) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Önceki ay")
            }
            Text(
                formatMonth(month),
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onNext, enabled = canGoForward, colors = arrowColors) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
            }
        }
        Row(Modifier.clearAndSetSemantics {}) {
            weekdayInitials.forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Yap.TextFaint)
            }
        }
        monthWeeks(month).forEach { week ->
            Row {
                week.forEach { day ->
                    if (day == null) {
                        Box(Modifier.weight(1f))
                    } else {
                        DayCell(
                            day = day,
                            status = dayStatus(day, done, today, startDay),
                            part = if (day in done) chainPart(day, done) else null,
                            label = dayLabel(day, today),
                            toggleable = canToggle(day, today),
                            onToggle = { onToggle(day, it) },
                        )
                    }
                }
            }
        }
    }
}

private fun ChainPart.shape(r: Dp): Shape = when (this) {
    ChainPart.SINGLE -> RoundedCornerShape(r)
    ChainPart.START -> RoundedCornerShape(topStart = r, bottomStart = r)
    ChainPart.MIDDLE -> RectangleShape
    ChainPart.END -> RoundedCornerShape(topEnd = r, bottomEnd = r)
}

private fun DayStatus.spoken(): String = when (this) {
    DayStatus.DONE -> "yapıldı"
    DayStatus.TODAY -> "henüz yapılmadı"
    DayStatus.MISSED -> "yapılmadı"
    DayStatus.FUTURE, DayStatus.NOT_STARTED -> ""
}

@Composable
private fun RowScope.DayCell(
    day: LocalDate,
    status: DayStatus,
    part: ChainPart?,
    label: String,
    toggleable: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val description = listOf(label, status.spoken()).filter { it.isNotEmpty() }.joinToString(", ")
    Box(
        Modifier
            .weight(1f)
            .height(48.dp)
            .then(
                if (toggleable) {
                    Modifier.toggleable(value = status == DayStatus.DONE, role = Role.Checkbox, onValueChange = onToggle)
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        val inner = Modifier.fillMaxSize()
        val box = when (status) {
            DayStatus.DONE -> inner.background(Yap.Accent, part!!.shape(cellCorner))
            DayStatus.TODAY -> inner
                .padding(horizontal = 3.dp)
                .border(1.5.dp, Yap.Accent, RoundedCornerShape(cellCorner))
            DayStatus.MISSED -> inner
                .padding(horizontal = 3.dp)
                .clip(RoundedCornerShape(cellCorner))
                .background(Yap.OverdueRow)
            DayStatus.FUTURE, DayStatus.NOT_STARTED -> inner
        }
        Box(box, contentAlignment = Alignment.Center) {
            Text(
                "${day.dayOfMonth}",
                fontFamily = PlexMono,
                fontSize = 14.sp,
                fontWeight = if (status == DayStatus.DONE || status == DayStatus.TODAY) FontWeight.SemiBold else FontWeight.Normal,
                color = when (status) {
                    DayStatus.DONE -> Yap.OnAccent
                    DayStatus.TODAY -> Yap.Accent
                    DayStatus.MISSED -> Yap.TextFaint
                    DayStatus.FUTURE, DayStatus.NOT_STARTED -> Yap.EmptyIcon
                },
            )
        }
    }
}
