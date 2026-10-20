package com.gloster.yaplan.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.theme.YapLanTheme
import kotlinx.coroutines.launch

/** Shown when the widget is added from the home screen's widget list: which habit should it show? */
class HabitWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Backing out must not leave an empty widget on the home screen.
        setResult(RESULT_CANCELED, result)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val habits = (application as YapLanApp).habits

        setContent {
            YapLanTheme {
                val list by habits.allHabits.collectAsStateWithLifecycle(initialValue = null)
                HabitPicker(
                    habits = list,
                    onPick = { habit ->
                        lifecycleScope.launch {
                            HabitWidget.assign(this@HabitWidgetConfigActivity, appWidgetId, habit.id)
                            setResult(RESULT_OK, result)
                            finish()
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

@Composable
private fun HabitPicker(habits: List<Habit>?, onPick: (Habit) -> Unit, onOpenApp: () -> Unit, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Yap.Background, contentColor = Yap.Text) {
        Column(Modifier.systemBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "Kapat")
                }
                Text(
                    "Widget için alışkanlık seç",
                    modifier = Modifier.semantics { heading() },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = Yap.Divider)

            when {
                habits == null -> Unit
                habits.isEmpty() -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, tint = Yap.EmptyIcon, modifier = Modifier.size(56.dp))
                    Text("Henüz alışkanlık yok", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Önce uygulamada bir alışkanlık ekle, sonra widget'ı yeniden ekle.",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Yap.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                    PrimaryButton("Alışkanlık ekle", onClick = onOpenApp, modifier = Modifier.padding(top = 14.dp))
                }
                else -> LazyColumn {
                    items(habits, key = { it.id }) { habit ->
                        Text(
                            habit.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(habit) }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        HorizontalDivider(color = Yap.Divider)
                    }
                }
            }
        }
    }
}
