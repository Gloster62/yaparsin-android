package com.gloster.yaplan.ui.components

import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gloster.yaplan.ui.theme.Yap

@Composable
fun yapSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = Yap.OnAccent,
    checkedTrackColor = Yap.Accent,
    checkedBorderColor = Yap.Accent,
    uncheckedThumbColor = Yap.CheckboxBorder,
    uncheckedTrackColor = Color.Transparent,
    uncheckedBorderColor = Yap.CheckboxBorder,
    disabledUncheckedThumbColor = Yap.OutlineStrong,
    disabledUncheckedTrackColor = Color.Transparent,
    disabledUncheckedBorderColor = Yap.OutlineStrong,
)
