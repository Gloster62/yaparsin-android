package com.gloster.yaplan.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.theme.Yap

/**
 * Starts a widget configuration screen and returns the widget's id, or null (and finishes) when
 * there is none. Until [finishWidgetConfig], backing out must not leave an empty widget behind.
 */
internal fun ComponentActivity.startWidgetConfig(): Int? {
    val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    setResult(Activity.RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
        finish()
        return null
    }
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    return appWidgetId
}

internal fun ComponentActivity.finishWidgetConfig(appWidgetId: Int) {
    setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    finish()
}

/** What a picker shows when there is nothing to pick yet. */
internal data class PickerEmpty(val icon: ImageVector, val title: String, val text: String, val button: String)

@Composable
internal fun <T> WidgetPicker(
    title: String,
    items: List<T>?,
    key: (T) -> Any,
    label: (T) -> String,
    empty: PickerEmpty,
    onPick: (T) -> Unit,
    onOpenApp: () -> Unit,
    onClose: () -> Unit,
    detail: (T) -> String? = { null },
) {
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
                    title,
                    modifier = Modifier.semantics { heading() },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = Yap.Divider)

            when {
                items == null -> Unit
                items.isEmpty() -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                ) {
                    Icon(empty.icon, contentDescription = null, tint = Yap.EmptyIcon, modifier = Modifier.size(56.dp))
                    Text(empty.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        empty.text,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Yap.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                    PrimaryButton(empty.button, onClick = onOpenApp, modifier = Modifier.padding(top = 14.dp))
                }
                else -> LazyColumn {
                    items(items, key = key) { item ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(item) }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                label(item),
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            detail(item)?.let { Text(it, fontSize = 13.sp, color = Yap.TextMuted) }
                        }
                        HorizontalDivider(color = Yap.Divider)
                    }
                }
            }
        }
    }
}
