package com.gloster.yaplan.ui.edit

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.nextCategoryColor
import com.gloster.yaplan.data.Priority
import com.gloster.yaplan.ui.categories.CategoryEditorSheet
import com.gloster.yaplan.ui.components.CategoryDot
import com.gloster.yaplan.ui.components.ChoiceChip
import com.gloster.yaplan.ui.components.FieldLabel
import com.gloster.yaplan.ui.components.LabeledTextField
import com.gloster.yaplan.ui.components.PriorityBars
import com.gloster.yaplan.ui.components.SelectableBox
import com.gloster.yaplan.ui.components.yapSwitchColors
import com.gloster.yaplan.ui.dayLabel
import com.gloster.yaplan.ui.formatShortDate
import com.gloster.yaplan.ui.formatTime
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

private val presetTimes = listOf(9, 12, 14, 18, 20).map { LocalTime.of(it, 0) }

/** Shared by the "Yeni görev" sheet and the edit screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    state: TaskEditState,
    categories: List<Category>,
    actions: TaskFormActions,
    modifier: Modifier = Modifier,
    fieldColor: Color = Yap.Background,
    autoFocusTitle: Boolean = false,
) {
    val context = LocalContext.current
    val scheduler = (context.applicationContext as YapLanApp).scheduler
    val today = remember { LocalDate.now() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showExactAlarmInfo by rememberSaveable { mutableStateOf(false) }
    var showNewCategory by rememberSaveable { mutableStateOf(false) }
    val titleFocus = remember { FocusRequester() }

    if (autoFocusTitle) {
        LaunchedEffect(Unit) { titleFocus.requestFocus() }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LabeledTextField(
            label = "Başlık",
            value = state.title,
            onValueChange = actions::setTitle,
            fieldModifier = Modifier.focusRequester(titleFocus),
            placeholder = "Ne yapacaksın?",
            containerColor = fieldColor,
            isError = !state.isNew && !state.canSave,
            errorText = "Başlık boş olamaz.",
        )
        LabeledTextField(
            label = "Not",
            labelHint = "(isteğe bağlı)",
            value = state.note,
            onValueChange = actions::setNote,
            placeholder = "Ayrıntı ekle",
            singleLine = false,
            minLines = 2,
            containerColor = fieldColor,
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldLabel("Öncelik")
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    SelectableBox(
                        selected = state.priority == p,
                        onClick = { actions.setPriority(p) },
                        modifier = Modifier.weight(1f),
                    ) {
                        PriorityBars(p)
                        Text(p.label, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            FieldLabel("Kategori")
            FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(selected = state.categoryId == null, onClick = { actions.setCategory(null) }) {
                    Text("Yok")
                }
                categories.forEach { c ->
                    ChoiceChip(selected = state.categoryId == c.id, onClick = { actions.setCategory(c.id) }) {
                        CategoryDot(Color(c.color), size = 9.dp)
                        Text(c.name)
                    }
                }
                NewCategoryChip(onClick = { showNewCategory = true })
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Son tarih")
            val date = state.dueDate
            val isOther = date != null && date != today && date != today.plusDays(1)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateOption("Yok", date == null) { actions.setDueDate(null) }
                DateOption("Bugün", date == today) { actions.setDueDate(today) }
                DateOption("Yarın", date == today.plusDays(1)) { actions.setDueDate(today.plusDays(1)) }
                DateOption(if (isOther) formatShortDate(date) else "Başka gün", isOther) { showDatePicker = true }
            }
        }

        if (state.hasDate) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Saat")
                val isCustom = state.dueTime !in presetTimes
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetTimes.forEach { t ->
                        SelectableBox(
                            selected = state.dueTime == t,
                            onClick = { actions.setDueTime(t) },
                            modifier = Modifier.weight(1f),
                            height = 44.dp,
                            horizontalPadding = 0.dp,
                        ) {
                            Text(formatTime(t), fontFamily = PlexMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    SelectableBox(
                        selected = isCustom,
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1.2f),
                        height = 44.dp,
                        horizontalPadding = 0.dp,
                    ) {
                        if (isCustom) {
                            Text(formatTime(state.dueTime), fontFamily = PlexMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Diğer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        ReminderSwitch(
            state = state,
            today = today,
            onChange = {
                actions.setRemind(it)
                if (it && !scheduler.canScheduleExact()) showExactAlarmInfo = true
            },
        )
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = (state.dueDate ?: today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        actions.setDueDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } },
            colors = DatePickerDefaults.colors(containerColor = Yap.DialogSurface),
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(containerColor = Yap.DialogSurface),
            )
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(state.dueTime.hour, state.dueTime.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Saat seç") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    actions.setDueTime(LocalTime.of(timeState.hour, timeState.minute))
                    showTimePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("İptal") } },
        )
    }

    if (showExactAlarmInfo) {
        AlertDialog(
            onDismissRequest = { showExactAlarmInfo = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Tam zamanlı hatırlatma") },
            text = {
                Text(
                    "Hatırlatmanın tam zamanında gelmesi için “Alarmlar ve hatırlatıcılar” " +
                        "iznini açman gerekiyor. İzin olmadan bildirim birkaç dakika gecikebilir.",
                    color = Yap.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmInfo = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData("package:${context.packageName}".toUri()),
                        )
                    }
                }) { Text("Ayarlara git", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmInfo = false }) { Text("Şimdi değil", color = Yap.Text) }
            },
        )
    }

    if (showNewCategory) {
        CategoryEditorSheet(
            initial = Category(name = "", color = nextCategoryColor(categories)),
            onDismiss = { showNewCategory = false },
            onSave = {
                actions.createCategory(it)
                showNewCategory = false
            },
        )
    }
}

@Composable
private fun RowScope.DateOption(label: String, selected: Boolean, onClick: () -> Unit) {
    SelectableBox(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.weight(1f),
        height = 44.dp,
        horizontalPadding = 4.dp,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun NewCategoryChip(onClick: () -> Unit) {
    Box(
        Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .height(36.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Yap.CheckboxBorder,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 3.dp.toPx())),
                        ),
                    )
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Yap.TextSecondary, modifier = Modifier.size(16.dp))
            Text("Yeni kategori", color = Yap.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ReminderSwitch(state: TaskEditState, today: LocalDate, onChange: (Boolean) -> Unit) {
    val enabled = state.hasDate
    val hint = when {
        !enabled -> "Önce son tarih seç"
        state.remind -> "Bildirim gelecek: ${dayLabel(state.dueDate!!, today)} ${formatTime(state.dueTime)}"
        else -> "Kapalı"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .background(Yap.Surface, RoundedCornerShape(6.dp))
            .border(1.dp, Yap.Border, RoundedCornerShape(6.dp))
            .toggleable(value = state.remind, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = null,
            tint = if (state.remind) Yap.Accent else Yap.TextMuted,
            modifier = Modifier.size(20.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Hatırlat",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Medium,
                color = if (enabled) Yap.Text else Yap.TextMuted,
            )
            Text(hint, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Yap.TextMuted)
        }
        Switch(
            checked = state.remind,
            onCheckedChange = null,
            enabled = enabled,
            colors = yapSwitchColors(),
        )
    }
}
