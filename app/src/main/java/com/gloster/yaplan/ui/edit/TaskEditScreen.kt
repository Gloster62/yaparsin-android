package com.gloster.yaplan.ui.edit

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Priority
import com.gloster.yaplan.ui.formatDateTime
import com.gloster.yaplan.ui.list.ColorDot
import com.gloster.yaplan.ui.theme.color
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    onBack: () -> Unit,
    viewModel: TaskEditViewModel = viewModel(factory = TaskEditViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scheduler = (context.applicationContext as YapLanApp).scheduler

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var pickedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var showExactAlarmInfo by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "Yeni görev" else "Görevi düzenle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    }
                    IconButton(onClick = { viewModel.save(onBack) }, enabled = state.canSave) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet")
                    }
                },
            )
        },
    ) { padding ->
        if (!state.loaded) return@Scaffold
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = { Text("Not") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )

            Section("Öncelik") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = state.priority == p,
                            onClick = { viewModel.setPriority(p) },
                            label = { Text(p.label) },
                            leadingIcon = { ColorDot(p.color) },
                        )
                    }
                }
            }

            Section("Kategori") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = state.categoryId == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("Yok") },
                    )
                    categories.forEach { c ->
                        FilterChip(
                            selected = state.categoryId == c.id,
                            onClick = { viewModel.setCategory(c.id) },
                            label = { Text(c.name) },
                            leadingIcon = { ColorDot(Color(c.color)) },
                        )
                    }
                }
            }

            Section("Son tarih") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = { Text(state.dueAt?.let(::formatDateTime) ?: "Tarih seç") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    )
                    if (state.dueAt != null) {
                        IconButton(onClick = { viewModel.setDueAt(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Tarihi kaldır")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = state.remind,
                            enabled = state.dueAt != null,
                            role = Role.Switch,
                            onValueChange = {
                                viewModel.setRemind(it)
                                if (it && !scheduler.canScheduleExact()) showExactAlarmInfo = true
                            },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Hatırlat",
                        modifier = Modifier.weight(1f),
                        color = if (state.dueAt == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Switch(
                        checked = state.remind,
                        enabled = state.dueAt != null,
                        onCheckedChange = null,
                    )
                }
            }

            Button(
                onClick = { viewModel.save(onBack) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Kaydet")
            }
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueAt?.let(::localToUtcDate)
                ?: LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickedDate = dateState.selectedDateMillis
                    showDatePicker = false
                }) { Text("İleri") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("İptal") } },
        ) {
            DatePicker(state = dateState)
        }
    }

    pickedDate?.let { utcDate ->
        val initial = state.dueAt?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
            ?: LocalTime.of(9, 0)
        val timeState = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickedDate = null },
            title = { Text("Saat seç") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val date = Instant.ofEpochMilli(utcDate).atZone(ZoneOffset.UTC).toLocalDate()
                    val dateTime = LocalDateTime.of(date, LocalTime.of(timeState.hour, timeState.minute))
                    viewModel.setDueAt(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    pickedDate = null
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { pickedDate = null }) { Text("İptal") } },
        )
    }

    if (showExactAlarmInfo) {
        AlertDialog(
            onDismissRequest = { showExactAlarmInfo = false },
            title = { Text("Tam zamanlı hatırlatma") },
            text = {
                Text(
                    "Hatırlatmanın tam zamanında gelmesi için \"Alarmlar ve hatırlatıcılar\" " +
                        "iznini açman gerekiyor. İzin olmadan bildirim birkaç dakika gecikebilir.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmInfo = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(android.net.Uri.parse("package:${context.packageName}")),
                        )
                    }
                }) { Text("Ayarlara git") }
            },
            dismissButton = { TextButton(onClick = { showExactAlarmInfo = false }) { Text("Şimdi değil") } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Görev silinsin mi?") },
            text = { Text("\"${state.title}\" kalıcı olarak silinecek.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onBack)
                }) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

/** Yerel saatteki bir anı, DatePicker'ın beklediği UTC gece yarısına çevirir. */
private fun localToUtcDate(millis: Long): Long =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
