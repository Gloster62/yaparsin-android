package com.gloster.yaplan.ui.habits

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.ui.components.FieldLabel
import com.gloster.yaplan.ui.components.LabeledTextField
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.components.SelectableBox
import com.gloster.yaplan.ui.components.yapSwitchColors
import com.gloster.yaplan.ui.formatTime
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import java.time.LocalTime
import kotlinx.coroutines.launch

private val presetMinutes = listOf(8, 12, 18, 21).map { it * 60 }
private const val DEFAULT_MINUTES = 21 * 60

private fun Int.asTime(): LocalTime = LocalTime.of(this / 60, this % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorSheet(
    initial: Habit,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var remind by rememberSaveable { mutableStateOf(initial.reminderMinutes != null) }
    var minutes by rememberSaveable { mutableIntStateOf(initial.reminderMinutes ?: DEFAULT_MINUTES) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var alarm by rememberSaveable { mutableStateOf(initial.reminderAlarm) }
    var showFullScreenInfo by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val nameFocus = remember { FocusRequester() }
    if (initial.id == 0L) {
        LaunchedEffect(Unit) { nameFocus.requestFocus() }
    }

    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Yap.SheetSurface,
        contentColor = Yap.Text,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Yap.OutlineStrong, RoundedCornerShape(2.dp)),
            )
        },
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (initial.id == 0L) "Yeni alışkanlık" else "Alışkanlığı düzenle",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                fontWeight = FontWeight.SemiBold,
            )

            LabeledTextField(
                label = "Ad",
                value = name,
                onValueChange = { name = it },
                fieldModifier = Modifier.focusRequester(nameFocus),
                placeholder = "Örn. Kitap oku",
            )

            ReminderToggle(remind = remind, minutes = minutes, onChange = { remind = it })

            if (remind) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel("Saat")
                    val isCustom = minutes !in presetMinutes
                    Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetMinutes.forEach { m ->
                            SelectableBox(
                                selected = minutes == m,
                                onClick = { minutes = m },
                                modifier = Modifier.weight(1f),
                                height = 44.dp,
                                horizontalPadding = 0.dp,
                            ) {
                                Text(formatTime(m.asTime()), fontFamily = PlexMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                Text(formatTime(minutes.asTime()), fontFamily = PlexMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("Diğer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (remind) {
                ReminderStyle(
                    alarm = alarm,
                    onChange = {
                        alarm = it
                        if (it && !canUseFullScreen(context)) showFullScreenInfo = true
                    },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("İptal", onClick = { close(onDismiss) }, modifier = Modifier.weight(1f))
                PrimaryButton(
                    "Kaydet",
                    onClick = {
                        close {
                            onSave(
                                initial.copy(
                                    name = name.trim(),
                                    reminderMinutes = minutes.takeIf { remind },
                                    reminderAlarm = alarm && remind,
                                ),
                            )
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(minutes / 60, minutes % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Saat seç") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    minutes = timeState.hour * 60 + timeState.minute
                    showTimePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("İptal") } },
        )
    }

    if (showFullScreenInfo) {
        AlertDialog(
            onDismissRequest = { showFullScreenInfo = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Tam ekran izni") },
            text = {
                Text(
                    "Alarmın kilit ekranında tam ekran açılması için “Tam ekran bildirimleri” iznini açman " +
                        "gerekiyor. İzin olmadan alarm, sesli bir bildirim olarak gelir.",
                    color = Yap.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showFullScreenInfo = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                                .setData("package:${context.packageName}".toUri()),
                        )
                    }
                }) { Text("Ayarlara git", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showFullScreenInfo = false }) { Text("Şimdi değil", color = Yap.Text) }
            },
        )
    }
}

@Composable
private fun ReminderToggle(remind: Boolean, minutes: Int, onChange: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .background(Yap.Surface, shape)
            .border(1.dp, Yap.Border, shape)
            .toggleable(value = remind, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = null,
            tint = if (remind) Yap.Accent else Yap.TextMuted,
            modifier = Modifier.size(20.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Her gün hatırlat",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Medium,
            )
            Text(
                if (remind) "Saat ${formatTime(minutes.asTime())}, o gün yapılmadıysa" else "Kapalı",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Yap.TextMuted,
            )
        }
        Switch(checked = remind, onCheckedChange = null, colors = yapSwitchColors())
    }
}

/** Android 14 made full-screen alarms a permission the user can switch off per app. */
private fun canUseFullScreen(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

@Composable
private fun ReminderStyle(alarm: Boolean, onChange: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Nasıl hatırlatayım?")
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectableBox(selected = !alarm, onClick = { onChange(false) }, modifier = Modifier.weight(1f), height = 44.dp) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Bildirim", fontWeight = FontWeight.SemiBold)
            }
            SelectableBox(selected = alarm, onClick = { onChange(true) }, modifier = Modifier.weight(1f), height = 44.dp) {
                Icon(Icons.Outlined.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Alarm", fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            if (alarm) "Tam ekran çalar; Yaptım, Ertele ya da Kapat diyene kadar susmaz." else "Bildirim olarak gelir.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Yap.TextMuted,
        )
    }
}
