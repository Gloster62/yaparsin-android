package com.gloster.yaplan.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.gloster.yaplan.YapLanApp

class WidgetPromoState(val visible: Boolean, val dismiss: () -> Unit)

/**
 * Suggest a widget while none of [receivers] is on the home screen and the card hasn't been
 * closed. Rechecked on every resume, since placing one happens outside the app.
 */
@Composable
fun rememberWidgetPromo(key: String, vararg receivers: Class<*>): WidgetPromoState {
    val context = LocalContext.current
    val prefs = (context.applicationContext as YapLanApp).prefs
    var dismissed by remember { mutableStateOf(prefs.isPromoDismissed(key)) }
    var placed by remember { mutableStateOf(receivers.any { hasWidget(context, it) }) }
    LifecycleResumeEffect(Unit) {
        placed = receivers.any { hasWidget(context, it) }
        onPauseOrDispose {}
    }
    return WidgetPromoState(visible = !dismissed && !placed) {
        prefs.dismissPromo(key)
        dismissed = true
    }
}
