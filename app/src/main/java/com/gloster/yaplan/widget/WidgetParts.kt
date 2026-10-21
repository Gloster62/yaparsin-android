package com.gloster.yaplan.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gloster.yaplan.R
import com.gloster.yaplan.ui.theme.Yap

internal fun color(c: Color) = ColorProvider(c)

@Composable
internal fun WidgetMessage(text: String) {
    Text(
        text,
        style = TextStyle(color = color(Yap.TextMuted), fontSize = 14.sp, textAlign = TextAlign.Center),
    )
}

/** The app's square checkbox; the caller's clickable parent carries the description. */
@Composable
internal fun WidgetCheckbox(checked: Boolean, overdue: Boolean = false) {
    val drawable = when {
        checked -> R.drawable.widget_check_on
        overdue -> R.drawable.widget_check_overdue
        else -> R.drawable.widget_check_off
    }
    Image(ImageProvider(drawable), contentDescription = null, modifier = GlanceModifier.size(20.dp))
}
