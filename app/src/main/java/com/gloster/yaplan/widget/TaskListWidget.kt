package com.gloster.yaplan.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.ChecklistProgress
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.ui.compactDueLabel
import com.gloster.yaplan.ui.list.dailyProgress
import com.gloster.yaplan.ui.list.widgetTasks
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.LocalDate
import java.time.ZoneId

/** Open tasks with today's progress; tick from the home screen, "+" opens "Yeni görev". */
class TaskListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as YapLanApp).repository
        provideContent {
            val tasks by repository.allTasks.collectAsState(null)
            val checklist by repository.checklistProgress.collectAsState(emptyMap())
            TaskListWidgetContent(tasks, checklist, System.currentTimeMillis(), ZoneId.systemDefault())
        }
    }
}

@Composable
private fun TaskListWidgetContent(tasks: List<Task>?, checklist: Map<Long, ChecklistProgress>, now: Long, zone: ZoneId) {
    val context = LocalContext.current
    val today = now.toLocalDateTime(zone).toLocalDate()
    Column(
        GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(6.dp),
    ) {
        if (tasks == null) return@Column
        val shown = widgetTasks(tasks, now, zone)
        val progress = dailyProgress(tasks, now, zone)
        val openCount = shown.count { !it.isDone }

        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                GlanceModifier
                    .defaultWeight()
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    .clickable(actionStartActivity(DeepLinks.intent(context, DeepLinks.tasks))),
            ) {
                Text(
                    "Yapılacaklar · $openCount",
                    style = TextStyle(color = color(Yap.Text), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Text(
                    if (progress.total == 0) "Bugün için görev yok" else "Bugün ${progress.done}/${progress.total} bitti",
                    style = TextStyle(color = color(if (progress.total > 0 && progress.left == 0) Yap.Accent else Yap.TextMuted), fontSize = 12.sp),
                    maxLines = 1,
                )
            }
            Box(
                GlanceModifier
                    .size(44.dp)
                    .clickable(actionStartActivity(DeepLinks.intent(context, DeepLinks.newTask)))
                    .semantics { contentDescription = "Görev ekle" },
                contentAlignment = Alignment.Center,
            ) {
                Image(ImageProvider(R.drawable.widget_add), contentDescription = null, modifier = GlanceModifier.size(32.dp))
            }
        }
        Spacer(GlanceModifier.height(2.dp))
        if (shown.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WidgetMessage("Yapılacak görev yok.\nEklemek için + ya dokun")
            }
        } else {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                items(shown, itemId = { it.id }) { task -> TaskLine(task, checklist[task.id], now, today) }
            }
        }
    }
}

@Composable
private fun TaskLine(task: Task, progress: ChecklistProgress?, now: Long, today: LocalDate) {
    val context = LocalContext.current
    val overdue = !task.isDone && task.dueAt != null && task.dueAt < now
    val trailing = listOfNotNull(
        progress?.let { "${it.done}/${it.total}" },
        task.dueAt?.let { compactDueLabel(it, today) },
    ).joinToString("  ")

    Row(GlanceModifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            GlanceModifier
                .size(44.dp)
                .clickable(setTaskDone(task.id, !task.isDone))
                .semantics {
                    contentDescription =
                        (if (task.isDone) "Yapılmadı olarak işaretle: " else "Tamamlandı olarak işaretle: ") + task.title
                },
            contentAlignment = Alignment.Center,
        ) {
            WidgetCheckbox(checked = task.isDone, overdue = overdue)
        }
        Row(
            GlanceModifier
                .defaultWeight()
                .height(44.dp)
                .padding(end = 8.dp)
                .clickable(actionStartActivity(DeepLinks.intent(context, DeepLinks.task(task.id)))),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                task.title,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = color(if (task.isDone) Yap.TextFaint else Yap.Text),
                    fontSize = 14.sp,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                ),
                maxLines = 1,
            )
            if (trailing.isNotEmpty()) {
                Text(
                    trailing,
                    modifier = GlanceModifier.padding(start = 6.dp),
                    style = TextStyle(
                        color = color(if (overdue) Yap.Danger else Yap.TextMuted),
                        fontSize = 12.sp,
                        fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}
