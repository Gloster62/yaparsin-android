package com.gloster.yaplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.theme.PlexMono
import com.gloster.yaplan.ui.theme.Yap

/** "3/6 bugün bitti" with one bar segment per item (a plain bar past [MAX_SEGMENTS]) and a nudge. */
@Composable
fun ProgressCard(done: Int, total: Int, label: String, message: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(Yap.Surface, shape)
            .border(1.dp, Yap.Border, shape)
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                buildAnnotatedString {
                    append("$done")
                    withStyle(SpanStyle(color = Yap.TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Medium)) {
                        append("/$total")
                    }
                },
                fontFamily = PlexMono,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(label, fontSize = 14.sp, color = Yap.TextMuted, modifier = Modifier.padding(bottom = 3.dp))
        }
        if (total in 1..MAX_SEGMENTS) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(total) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(if (i < done) Yap.Accent else Yap.Border, RoundedCornerShape(3.dp)),
                    )
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Yap.Border),
            ) {
                if (total > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth(done.toFloat() / total)
                            .height(6.dp)
                            .background(Yap.Accent),
                    )
                }
            }
        }
        Text(message, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    }
}

private const val MAX_SEGMENTS = 12
