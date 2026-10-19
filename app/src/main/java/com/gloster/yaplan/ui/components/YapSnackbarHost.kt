package com.gloster.yaplan.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gloster.yaplan.ui.theme.Yap

/**
 * One app-wide host state: a deletion's "Geri al" must survive leaving the edit screen, so the
 * snackbar is owned by the root and each tab screen just renders it.
 */
val LocalAppSnackbar = staticCompositionLocalOf<SnackbarHostState> { error("LocalAppSnackbar not provided") }

@Composable
fun YapSnackbarHost(modifier: Modifier = Modifier) {
    SnackbarHost(LocalAppSnackbar.current, modifier) { data ->
        val shape = RoundedCornerShape(6.dp)
        // The SnackbarData overload pads inside its modifier, which would put the border outside the box.
        Snackbar(
            modifier = Modifier
                .padding(12.dp)
                .border(1.dp, Yap.Outline, shape),
            action = data.visuals.actionLabel?.let { label ->
                {
                    TextButton(
                        onClick = data::performAction,
                        colors = ButtonDefaults.textButtonColors(contentColor = Yap.Accent),
                    ) { Text(label, fontWeight = FontWeight.Bold) }
                }
            },
            shape = shape,
            containerColor = Yap.DialogSurface,
            contentColor = Yap.Text,
        ) {
            Text(data.visuals.message)
        }
    }
}
