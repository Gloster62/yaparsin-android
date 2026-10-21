package com.gloster.yaplan.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.ui.theme.YapLanTheme
import kotlinx.coroutines.launch

/** Shown when the widget is added from the home screen's widget list: which habit should it show? */
class HabitWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = startWidgetConfig() ?: return
        val habits = (application as YapLanApp).habits

        setContent {
            YapLanTheme {
                val list by habits.allHabits.collectAsStateWithLifecycle(initialValue = null)
                WidgetPicker(
                    title = "Widget için alışkanlık seç",
                    items = list,
                    key = { it.id },
                    label = { it.name },
                    empty = PickerEmpty(
                        icon = Icons.Outlined.Link,
                        title = "Henüz alışkanlık yok",
                        text = "Önce uygulamada bir alışkanlık ekle, sonra widget'ı yeniden ekle.",
                        button = "Alışkanlık ekle",
                    ),
                    onPick = { habit ->
                        lifecycleScope.launch {
                            HabitWidget.assign(this@HabitWidgetConfigActivity, appWidgetId, habit.id)
                            finishWidgetConfig(appWidgetId)
                        }
                    },
                    onOpenApp = {
                        startActivity(DeepLinks.intent(this, DeepLinks.HABITS.toUri()))
                        finish()
                    },
                    onClose = ::finish,
                )
            }
        }
    }
}
