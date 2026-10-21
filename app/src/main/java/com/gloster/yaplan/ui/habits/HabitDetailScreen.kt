package com.gloster.yaplan.ui.habits

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.ui.components.LocalAppSnackbar
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.components.YapSnackbarHost
import com.gloster.yaplan.ui.dayLabel
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.widget.ADD_WIDGET_BY_HAND
import com.gloster.yaplan.widget.requestHabitWidget
import kotlinx.coroutines.launch

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    viewModel: HabitDetailViewModel = viewModel(factory = HabitDetailViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalAppSnackbar.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val habit = state.habit

    BackHandler(onBack = onBack)
    LaunchedEffect(state.loaded, habit == null) {
        if (state.loaded && habit == null) onBack()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Yap.Background),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(
                    habit?.name.orEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (habit != null) {
                    IconButton(onClick = { editing = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Düzenle")
                    }
                }
            }
            HorizontalDivider(color = Yap.Divider)

            if (habit == null) return@Column

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StreakCard(state)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonthCalendar(
                        month = state.month,
                        today = state.today,
                        done = state.done,
                        startDay = state.startDay,
                        canGoBack = state.canGoBack,
                        canGoForward = state.canGoForward,
                        onPrevious = viewModel::previousMonth,
                        onNext = viewModel::nextMonth,
                        onToggle = viewModel::setDone,
                    )
                    Text(
                        "Yalnızca bugünü ve dünü işaretleyebilirsin.",
                        fontSize = 12.sp,
                        color = Yap.TextFaint,
                    )
                }
                SecondaryButton(
                    "Widget olarak ekle",
                    onClick = {
                        if (!requestHabitWidget(context, habit.id)) {
                            scope.launch {
                                snackbar.showSnackbar(ADD_WIDGET_BY_HAND)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leading = { Icon(Icons.Outlined.Widgets, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
                SecondaryButton(
                    "Alışkanlığı sil",
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = Yap.Danger,
                    borderColor = Yap.DangerBorder,
                    leading = { Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
                Text(
                    "Başlangıç: ${dayLabel(state.startDay, state.today)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Yap.TextFaint,
                )
            }

            HorizontalDivider(color = Yap.Border)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Yap.Surface)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
            ) {
                if (state.doneToday) {
                    SecondaryButton(
                        "Bugün yapıldı · geri al",
                        onClick = { viewModel.setDone(state.today, false) },
                        modifier = Modifier.fillMaxWidth(),
                        leading = { Icon(Icons.Default.Check, contentDescription = null, tint = Yap.Accent, modifier = Modifier.size(20.dp)) },
                    )
                } else {
                    PrimaryButton(
                        "Bugün yaptım",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            viewModel.setDone(state.today, true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leading = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    )
                }
            }
        }
        YapSnackbarHost(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }

    if (editing && habit != null) {
        HabitEditorSheet(
            initial = habit,
            onDismiss = { editing = false },
            onSave = {
                viewModel.save(it)
                editing = false
            },
        )
    }

    if (confirmDelete && habit != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Alışkanlık silinsin mi?") },
            text = {
                Text(
                    "“${habit.name}” ve tüm zincir geçmişi silinecek. Bu geri alınamaz.",
                    color = Yap.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Sil", color = Yap.Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Vazgeç", color = Yap.Text) } },
        )
    }
}

@Composable
private fun StreakCard(state: HabitDetailUiState) {
    val shape = RoundedCornerShape(6.dp)
    val message = when {
        state.doneToday -> "Bugünkü halka eklendi. Yarın da gel!"
        state.atRisk -> "Bugün yapmazsan zincir kırılır!"
        state.longest > 0 -> "Zincir kırıldı. Bugün yeniden başla!"
        else -> "Bugün yap, zinciri başlat."
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Yap.Surface, shape)
            .border(1.dp, Yap.Border, shape)
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${state.streak}",
                fontFamily = PlexMono,
                fontSize = 40.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (state.streak > 0) Yap.Accent else Yap.Text,
            )
            Text("günlük zincir", fontSize = 14.sp, color = Yap.TextMuted, modifier = Modifier.padding(bottom = 6.dp))
        }
        Text(
            message,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (state.atRisk) Yap.Accent else Yap.Text,
        )
        HorizontalDivider(color = Yap.Divider)
        Row {
            Stat("En uzun zincir", "${state.longest} gün", Modifier.weight(1f))
            Stat("Bu ay", "${state.thisMonth.done}/${state.thisMonth.days} gün", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = Yap.TextMuted)
        Text(value, fontFamily = PlexMono, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
