package com.gloster.yaplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.theme.Yap

/** A closable suggestion to put one of the app's widgets on the home screen. */
@Composable
fun WidgetPromoCard(
    title: String,
    body: String,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(Yap.AccentContainer, shape)
            .border(1.dp, Yap.Border, shape)
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.Widgets, contentDescription = null, tint = Yap.Accent, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(body, fontSize = 12.5.sp, lineHeight = 16.sp, color = Yap.TextMuted)
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yap.Accent, contentColor = Yap.OnAccent),
        ) {
            Text("Ekle", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Öneriyi kapat", tint = Yap.TextFaint, modifier = Modifier.size(18.dp))
        }
    }
}
