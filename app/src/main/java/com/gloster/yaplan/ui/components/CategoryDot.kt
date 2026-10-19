package com.gloster.yaplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    val corner = if (size > 12.dp) 3.dp else 2.dp
    Box(
        modifier
            .size(size)
            .background(color, RoundedCornerShape(corner)),
    )
}
