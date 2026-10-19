package com.gloster.yaplan.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.CategoryColors
import com.gloster.yaplan.ui.components.CategoryDot
import com.gloster.yaplan.ui.components.FieldLabel
import com.gloster.yaplan.ui.components.LabeledTextField
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.theme.Yap
import kotlinx.coroutines.launch

private val colorNames = listOf("Mavi", "Yeşil", "Turuncu", "Mor", "Kırmızı", "Turkuaz", "Pembe", "Kahverengi")

private fun colorName(color: Long): String = colorNames.getOrElse(CategoryColors.indexOf(color)) { "" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorSheet(
    initial: Category,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var color by rememberSaveable { mutableLongStateOf(initial.color) }
    val valid = name.isNotBlank()
    val nameFocus = remember { FocusRequester() }
    if (initial.id == 0L) {
        LaunchedEffect(Unit) { nameFocus.requestFocus() }
    }

    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Yap.SheetSurface,
        contentColor = Yap.Text,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Yap.OutlineStrong, RoundedCornerShape(2.dp)),
            )
        },
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (initial.id == 0L) "Yeni kategori" else "Kategoriyi düzenle",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Önizleme ", style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Yap.TextMuted)
                Row(
                    Modifier
                        .height(32.dp)
                        .border(1.dp, Yap.Outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    CategoryDot(Color(color), size = 9.dp)
                    Text(name.trim().ifEmpty { "Ad" }, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
                }
            }

            LabeledTextField(
                label = "Ad",
                value = name,
                onValueChange = { name = it },
                fieldModifier = Modifier.focusRequester(nameFocus),
                placeholder = "Örn. Hobi",
                capitalization = KeyboardCapitalization.Words,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    FieldLabel("Renk")
                    Text(" · ${colorName(color)}", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = Yap.TextMuted)
                }
                Row(Modifier.fillMaxWidth().selectableGroup()) {
                    CategoryColors.forEachIndexed { i, c ->
                        val selected = c == color
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .selectable(selected = selected, role = Role.RadioButton, onClick = { color = c })
                                .semantics { contentDescription = colorNames[i] },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .border(2.dp, if (selected) Yap.Text else Color.Transparent, RoundedCornerShape(6.dp))
                                    .padding(3.dp)
                                    .background(Color(c), RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Yap.OnAccent, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("İptal", onClick = { close(onDismiss) }, modifier = Modifier.weight(1f))
                PrimaryButton(
                    "Kaydet",
                    onClick = { close { onSave(initial.copy(name = name.trim(), color = color)) } },
                    enabled = valid,
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }
}

