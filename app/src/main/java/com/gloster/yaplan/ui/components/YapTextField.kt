package com.gloster.yaplan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.ui.theme.Yap

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
        color = Yap.TextSecondary,
    )
}

/** A label above an outlined field, as in the design (not Material's floating label). */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    labelHint: String? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    errorText: String? = null,
    containerColor: Color = Yap.Background,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            FieldLabel(label)
            if (labelHint != null) {
                Text(
                    " $labelHint",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Normal),
                    color = Yap.TextMuted,
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
            placeholder = { Text(placeholder, color = Yap.TextFaint) },
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(capitalization = capitalization),
            colors = yapTextFieldColors(containerColor),
        )
        if (isError && errorText != null) {
            Text(errorText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Yap.Danger)
        }
    }
}

@Composable
fun yapTextFieldColors(containerColor: Color = Yap.Background) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Yap.Accent,
    unfocusedBorderColor = Yap.OutlineStrong,
    errorBorderColor = Yap.Danger,
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    errorContainerColor = containerColor,
    cursorColor = Yap.Accent,
    focusedTextColor = Yap.Text,
    unfocusedTextColor = Yap.Text,
)
