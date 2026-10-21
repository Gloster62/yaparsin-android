package com.gloster.yaplan.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.ui.compactDueLabel
import com.gloster.yaplan.ui.list.TaskFilter
import com.gloster.yaplan.ui.list.TaskTab
import com.gloster.yaplan.ui.list.applyFilter
import com.gloster.yaplan.ui.theme.YapLanTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Shown when the task widget is added from the home screen's widget list: which task should it show? */
class TaskWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = startWidgetConfig() ?: return
        val openTasks = (application as YapLanApp).repository.allTasks
            .map { applyFilter(it, TaskFilter(tab = TaskTab.OPEN)) }
        val today = LocalDate.now()

        setContent {
            YapLanTheme {
                val list by openTasks.collectAsStateWithLifecycle(initialValue = null)
                WidgetPicker(
                    title = "Widget için görev seç",
                    items = list,
                    key = { it.id },
                    label = { it.title },
                    detail = { task -> task.dueAt?.let { compactDueLabel(it, today) } },
                    empty = PickerEmpty(
                        icon = Icons.Outlined.CheckBox,
                        title = "Yapılacak görev yok",
                        text = "Önce uygulamada bir görev ekle, sonra widget'ı yeniden ekle.",
                        button = "Görev ekle",
                    ),
                    onPick = { task ->
                        lifecycleScope.launch {
                            TaskWidget.assign(this@TaskWidgetConfigActivity, appWidgetId, task.id)
                            finishWidgetConfig(appWidgetId)
                        }
                    },
                    onOpenApp = {
                        startActivity(DeepLinks.intent(this, DeepLinks.newTask))
                        finish()
                    },
                    onClose = ::finish,
                )
            }
        }
    }
}
