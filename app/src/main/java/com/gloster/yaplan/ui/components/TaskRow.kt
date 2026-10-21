package com.gloster.yaplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.ChecklistProgress
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.repeatTime
import com.gloster.yaplan.ui.dueLabel
import com.gloster.yaplan.ui.formatRepeat
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import java.time.LocalDate

@Composable
fun TaskCheckbox(checked: Boolean, overdue: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    if (checked) {
        Box(modifier.size(20.dp).background(Yap.Accent, shape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Yap.OnAccent, modifier = Modifier.size(16.dp))
        }
    } else {
        Box(modifier.size(20.dp).border(1.5.dp, if (overdue) Yap.Danger else Yap.CheckboxBorder, shape))
    }
}

/**
 * One task line: title, optional note, then a meta line (category, due time or "Gecikti",
 * repeat days, list progress) and the priority on the right. [onToggle] null hides the checkbox
 * (search results).
 */
@Composable
fun TaskRow(
    task: Task,
    category: Category?,
    now: Long,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggle: (() -> Unit)? = null,
    progress: ChecklistProgress? = null,
    highlight: String = "",
) {
    val overdue = !task.isDone && task.dueAt != null && task.dueAt < now
    val done = task.isDone
    Column(modifier.background(if (overdue) Yap.OverdueRow else Yap.Surface)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (onToggle != null) {
                Box(
                    Modifier
                        .width(52.dp)
                        .fillMaxHeight()
                        .toggleable(value = done, role = Role.Checkbox, onValueChange = { onToggle() })
                        .clearAndSetSemantics {
                            contentDescription =
                                (if (done) "Yapılmadı olarak işaretle: " else "Tamamlandı olarak işaretle: ") + task.title
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    TaskCheckbox(checked = done, overdue = overdue)
                }
            }
            Row(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(
                        start = if (onToggle == null) 16.dp else 0.dp,
                        end = 14.dp,
                        top = 11.dp,
                        bottom = 11.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = highlighted(task.title, highlight),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
                        fontWeight = FontWeight.Medium,
                        color = if (done) Yap.TextFaint else Yap.Text,
                        textDecoration = if (done) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (task.note.isNotBlank()) {
                        Text(
                            text = highlighted(task.note, highlight),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = Yap.TextFaint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    TaskMeta(task, category, progress, overdue, today)
                }
                Row(
                    Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    PriorityBars(task.priority, dimmed = done)
                    Text(
                        task.priority.label,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = "Öncelik: ${task.priority.label}" },
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = task.priority.labelColor(dimmed = done),
                    )
                }
            }
        }
        HorizontalDivider(color = Yap.Divider)
    }
}

@Composable
private fun TaskMeta(task: Task, category: Category?, progress: ChecklistProgress?, overdue: Boolean, today: LocalDate) {
    val color = if (task.isDone) Yap.TextFaint else Yap.TextMuted
    val metaStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp)
    val repeats = task.repeatDays != 0 && !task.isDone
    if (category == null && task.dueAt == null && progress == null && !repeats) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (category != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryDot(Color(category.color))
                Text(category.name, style = metaStyle, color = color)
            }
        }
        val dueAt = task.dueAt
        if (dueAt != null && overdue) {
            Row(
                Modifier
                    .border(1.dp, Yap.Danger, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Yap.Danger, modifier = Modifier.size(13.dp))
                Text(
                    "Gecikti · ${dueLabel(dueAt, today)}",
                    style = metaStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = Yap.Danger,
                )
            }
        } else if (dueAt != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Text(dueLabel(dueAt, today), style = metaStyle, color = color)
            }
        }
        if (repeats) {
            Row(
                Modifier.semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Outlined.Repeat, contentDescription = "Düzenli hatırlatma", tint = color, modifier = Modifier.size(13.dp))
                Text(formatRepeat(task.repeatDays, task.repeatTime), style = metaStyle, color = color)
            }
        }
        if (progress != null) {
            Row(
                Modifier.clearAndSetSemantics {
                    contentDescription = "Liste: ${progress.total} maddeden ${progress.done} tanesi tamam"
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Outlined.Checklist, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text("${progress.done}/${progress.total}", style = metaStyle, fontFamily = PlexMono, color = color)
            }
        }
    }
}

/** Marks the first case-insensitive match of [query] like the search design (yellow on dark yellow). */
fun highlighted(text: String, query: String): AnnotatedString {
    val q = query.trim()
    val start = if (q.isEmpty()) -1 else text.indexOf(q, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(background = Yap.Highlight, color = Yap.HighlightText)) {
            append(text.substring(start, start + q.length))
        }
        append(text.substring(start + q.length))
    }
}
