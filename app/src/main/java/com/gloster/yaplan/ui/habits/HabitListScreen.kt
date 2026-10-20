package com.gloster.yaplan.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.ProgressCard
import com.gloster.yaplan.ui.components.TaskCheckbox
import com.gloster.yaplan.ui.components.YapSnackbarHost
import com.gloster.yaplan.ui.formatHeaderDate
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap
import java.time.LocalDate

@Composable
fun HabitListScreen(
    onOpenHabit: (Long) -> Unit,
    viewModel: HabitListViewModel = viewModel(factory = HabitListViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNewHabit by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = Yap.Background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { YapSnackbarHost() },
        floatingActionButton = {
            if (state.rows.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showNewHabit = true },
                    containerColor = Yap.Accent,
                    contentColor = Yap.OnAccent,
                    shape = RoundedCornerShape(6.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Alışkanlık ekle", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Header(state.today)
            if (!state.loaded) return@Column
            if (state.rows.isEmpty()) {
                FirstRunEmpty(onAdd = { showNewHabit = true }, modifier = Modifier.weight(1f))
                return@Column
            }
            ProgressCard(
                done = state.doneToday,
                total = state.rows.size,
                label = "bugün yapıldı",
                message = habitProgressMessage(state.doneToday, state.rows.size),
            )
            Box(Modifier.height(12.dp))
            HorizontalDivider(color = Yap.Divider)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                items(state.rows, key = { it.habit.id }) { row ->
                    HabitListRow(
                        row = row,
                        onOpen = { onOpenHabit(row.habit.id) },
                        onToggle = { viewModel.toggleToday(row) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (showNewHabit) {
        HabitEditorSheet(
            initial = Habit(name = ""),
            onDismiss = { showNewHabit = false },
            onSave = {
                viewModel.save(it)
                showNewHabit = false
            },
        )
    }
}

@Composable
private fun Header(today: LocalDate) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Alışkanlıklar",
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(formatHeaderDate(today), fontSize = 13.sp, color = Yap.TextMuted)
    }
}

@Composable
private fun HabitListRow(row: HabitRow, onOpen: () -> Unit, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    val (hint, hintColor) = when {
        row.doneToday -> "Bugün yapıldı" to Yap.TextMuted
        row.atRisk -> "Bugün yapmazsan zincir kırılır" to Yap.Accent
        row.everDone -> "Zincir kırıldı · bugün yeniden başla" to Yap.TextMuted
        else -> "Bugün başla" to Yap.TextMuted
    }
    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .width(44.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "${row.streak} günlük zincir" },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "${row.streak}",
                    fontFamily = PlexMono,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (row.streak > 0) Yap.Accent else Yap.TextFaint,
                )
                Text("gün", fontSize = 11.sp, lineHeight = 13.sp, color = Yap.TextFaint)
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    row.habit.name,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(hint, fontSize = 13.sp, lineHeight = 17.sp, color = hintColor)
            }
            Box(
                Modifier
                    .size(48.dp)
                    .toggleable(
                        value = row.doneToday,
                        role = Role.Checkbox,
                        onValueChange = { done ->
                            if (done) haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            onToggle()
                        },
                    )
                    .semantics { contentDescription = "“${row.habit.name}” bugün yapıldı" },
                contentAlignment = Alignment.Center,
            ) {
                TaskCheckbox(checked = row.doneToday, overdue = false)
            }
        }
        HorizontalDivider(color = Yap.Divider)
    }
}

@Composable
private fun FirstRunEmpty(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Icon(Icons.Outlined.Link, contentDescription = null, tint = Yap.EmptyIcon, modifier = Modifier.size(56.dp))
        Text("Henüz alışkanlık yok", fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Her gün yapmak istediğin bir şey ekle. Yaptığın her gün zincire bir halka ekler. Zinciri kırma!",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Yap.TextMuted,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            "İlk alışkanlığını ekle",
            onClick = onAdd,
            modifier = Modifier.padding(top = 14.dp),
            leading = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
        )
    }
}
