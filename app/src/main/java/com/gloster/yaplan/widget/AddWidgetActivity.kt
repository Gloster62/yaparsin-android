package com.gloster.yaplan.widget

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.R
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.theme.YapLanTheme

private enum class WidgetChoice(val title: String, val description: String, @DrawableRes val preview: Int) {
    TASK_LIST("Yapılacaklar", "Açık görevler; ana ekrandan işaretle", R.drawable.task_list_widget_preview),
    TASK("Görev", "Tek görev ve listesi, ör. market listesi", R.drawable.task_widget_preview),
    HABIT("Alışkanlık takvimi", "Bir alışkanlığın bu ayki zinciri", R.drawable.habit_widget_preview),
}

/** Opened from the launcher icon's "Widget ekle" shortcut. */
class AddWidgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            YapLanTheme {
                AddWidgetSheet(onPick = ::pin, onDismiss = ::finish)
            }
        }
    }

    private fun pin(choice: WidgetChoice) {
        val requested = when (choice) {
            WidgetChoice.TASK_LIST -> requestTaskListWidget(this)
            WidgetChoice.TASK -> requestTaskWidget(this)
            WidgetChoice.HABIT -> requestHabitWidget(this)
        }
        if (!requested) {
            Toast.makeText(this, ADD_WIDGET_BY_HAND, Toast.LENGTH_LONG).show()
        }
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWidgetSheet(onPick: (WidgetChoice) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Yap.SheetSurface,
        contentColor = Yap.Text,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Yap.OutlineStrong, RoundedCornerShape(2.dp)),
            )
        },
    ) {
        Column(Modifier.padding(bottom = 16.dp)) {
            Text(
                "Ana ekrana widget ekle",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .semantics { heading() },
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
            WidgetChoice.entries.forEach { choice ->
                HorizontalDivider(color = Yap.Divider)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(choice) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Image(
                        painterResource(choice.preview),
                        contentDescription = null,
                        modifier = Modifier
                            .width(88.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(choice.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(choice.description, fontSize = 13.sp, lineHeight = 18.sp, color = Yap.TextMuted)
                    }
                }
            }
        }
    }
}
