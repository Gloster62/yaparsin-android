package com.gloster.yaplan.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.ChecklistItem
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.data.repeatTime
import com.gloster.yaplan.ui.dueLabel
import com.gloster.yaplan.ui.formatRepeat
import com.gloster.yaplan.ui.theme.Yap
import java.time.LocalDate
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

private val TASK_ID = longPreferencesKey("task_id")

private sealed interface TaskContent {
    data object Loading : TaskContent
    data object Unassigned : TaskContent
    data object Deleted : TaskContent
    data class Ready(val task: Task, val items: List<ChecklistItem>) : TaskContent
}

/** One task with its list; the task and each list line can be ticked from the home screen. */
class TaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as YapLanApp).repository
        val configure = Intent(context, TaskWidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, GlanceAppWidgetManager(context).getAppWidgetId(id))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        provideContent {
            val taskId = currentState(TASK_ID)
            val content by remember(taskId) {
                if (taskId == null) {
                    flowOf(TaskContent.Unassigned)
                } else {
                    combine(repository.observeTask(taskId), repository.observeItems(taskId)) { task, items ->
                        if (task == null) TaskContent.Deleted else TaskContent.Ready(task, items)
                    }
                }
            }.collectAsState(TaskContent.Loading)
            TaskWidgetContent(content, configure)
        }
    }

    companion object {
        suspend fun assign(context: Context, appWidgetId: Int, taskId: Long) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId) { it[TASK_ID] = taskId }
            TaskWidget().update(context, glanceId)
        }
    }
}

@Composable
private fun TaskWidgetContent(content: TaskContent, configure: Intent) {
    Box(
        GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(6.dp)
            .then(if (content is TaskContent.Ready) GlanceModifier else GlanceModifier.clickable(actionStartActivity(configure))),
        contentAlignment = Alignment.Center,
    ) {
        when (content) {
            TaskContent.Loading -> Unit
            TaskContent.Unassigned -> WidgetMessage("Görev seçmek için dokun")
            TaskContent.Deleted -> WidgetMessage("Bu görev silinmiş.\nBaşka birini seçmek için dokun")
            is TaskContent.Ready -> ReadyTask(content.task, content.items)
        }
    }
}

@Composable
private fun ReadyTask(task: Task, items: List<ChecklistItem>) {
    val context = LocalContext.current
    val open = actionStartActivity(DeepLinks.intent(context, DeepLinks.task(task.id)))
    val now = System.currentTimeMillis()
    val overdue = !task.isDone && task.dueAt != null && task.dueAt < now

    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
            Column(
                GlanceModifier
                    .defaultWeight()
                    .padding(end = 6.dp, top = 4.dp, bottom = 4.dp)
                    .clickable(open),
            ) {
                Text(
                    task.title,
                    style = TextStyle(
                        color = color(if (task.isDone) Yap.TextFaint else Yap.Text),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    ),
                    maxLines = 2,
                )
                metaLine(task, LocalDate.now())?.let {
                    Text(
                        it,
                        style = TextStyle(color = color(if (overdue) Yap.Danger else Yap.TextMuted), fontSize = 12.sp),
                        maxLines = 1,
                    )
                }
            }
            if (items.isNotEmpty()) {
                Text(
                    "${items.count { it.isDone }}/${items.size}",
                    modifier = GlanceModifier.padding(end = 8.dp),
                    style = TextStyle(color = color(Yap.Accent), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        if (items.isNotEmpty()) {
            Spacer(GlanceModifier.height(2.dp))
            LazyColumn(GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(items, itemId = { it.id }) { item -> ItemRow(item) }
            }
        } else if (task.note.isNotBlank()) {
            Text(
                task.note,
                modifier = GlanceModifier.padding(start = 44.dp, end = 8.dp).clickable(open),
                style = TextStyle(color = color(Yap.TextFaint), fontSize = 13.sp),
                maxLines = 4,
            )
        }
    }
}

private fun metaLine(task: Task, today: LocalDate): String? {
    if (task.isDone) return "Tamamlandı"
    val parts = listOfNotNull(
        task.dueAt?.let { dueLabel(it, today) },
        formatRepeat(task.repeatDays, task.repeatTime).takeIf { task.repeatDays != 0 },
    )
    return parts.joinToString(" · ").ifEmpty { null }
}

@Composable
private fun ItemRow(item: ChecklistItem) {
    Row(
        GlanceModifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(setItemDone(item.id, !item.isDone))
            .semantics {
                contentDescription = (if (item.isDone) "Alınmadı olarak işaretle: " else "Alındı olarak işaretle: ") + item.text
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(GlanceModifier.width(12.dp))
        WidgetCheckbox(checked = item.isDone)
        Spacer(GlanceModifier.width(12.dp))
        Text(
            item.text,
            modifier = GlanceModifier.defaultWeight().padding(end = 8.dp),
            style = TextStyle(
                color = color(if (item.isDone) Yap.TextFaint else Yap.Text),
                fontSize = 14.sp,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
            ),
            maxLines = 1,
        )
    }
}
