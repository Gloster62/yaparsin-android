package com.gloster.yaplan.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.nextCategoryColor
import com.gloster.yaplan.ui.components.CategoryDot
import com.gloster.yaplan.ui.components.YapSnackbarHost
import com.gloster.yaplan.ui.theme.Yap

private fun TaskCounts.summary() = "$open yapılacak · $done bitti"

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<CategoryRow?>(null) }

    Scaffold(
        containerColor = Yap.Background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { YapSnackbarHost() },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Kategoriler",
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Yap.Accent)
                        .clickable(role = Role.Button) {
                            editing = Category(name = "", color = nextCategoryColor(state.rows.map { it.category }))
                        }
                        .padding(start = 10.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Yap.OnAccent, modifier = Modifier.size(18.dp))
                    Text("Yeni kategori", color = Yap.OnAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Görevlerini gruplamak için kullan. Düzenlemek için bir kategoriye dokun.",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Yap.TextMuted,
            )
            HorizontalDivider(color = Yap.Divider)

            if (!state.loaded) return@Column
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                if (state.rows.isEmpty()) {
                    item {
                        Text(
                            "Henüz kategori yok. “Yeni kategori” ile ekle.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 15.sp,
                            color = Yap.TextMuted,
                        )
                    }
                }
                items(state.rows, key = { it.category.id }) { row ->
                    CategoryListRow(
                        row = row,
                        onEdit = { editing = row.category },
                        onDelete = { deleting = row },
                    )
                }
                item(key = "uncategorized") {
                    UncategorizedRow(state.uncategorized)
                }
            }
        }
    }

    editing?.let { category ->
        CategoryEditorSheet(
            initial = category,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            },
        )
    }

    deleting?.let { row ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = Yap.DialogSurface,
            title = { Text("“${row.category.name}” silinsin mi?") },
            text = {
                Text(
                    "Görevlerin silinmez. Bu kategorideki ${row.counts.total} görev “Kategorisiz” olur.",
                    color = Yap.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(row.category)
                    deleting = null
                }) { Text("Sil", color = Yap.Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Vazgeç", color = Yap.Text) } },
        )
    }
}

@Composable
private fun CategoryListRow(row: CategoryRow, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = Color(row.category.color)
    Column(Modifier.background(Yap.Surface)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 68.dp)
                    .clickable(onClick = onEdit)
                    .clearAndSetSemantics {
                        contentDescription = "${row.category.name} kategorisini düzenle, ${row.counts.summary()}"
                    }
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CategoryDot(color, size = 16.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(row.category.name, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(row.counts.summary(), fontSize = 13.sp, color = Yap.TextMuted)
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Yap.Border),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(row.counts.doneFraction)
                                .height(4.dp)
                                .background(color),
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.padding(end = 4.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "${row.category.name} kategorisini sil", tint = Yap.TextMuted)
            }
        }
        HorizontalDivider(color = Yap.Divider)
    }
}

@Composable
private fun UncategorizedRow(counts: TaskCounts) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .padding(horizontal = 16.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(16.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Yap.CheckboxBorder,
                            cornerRadius = CornerRadius(3.dp.toPx()),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                            ),
                        )
                    },
            )
            Text("Kategorisiz", modifier = Modifier.weight(1f), fontSize = 15.sp, color = Yap.TextSecondary)
            Text(counts.summary(), fontSize = 13.sp, color = Yap.TextMuted)
        }
        HorizontalDivider(color = Yap.Divider)
    }
}
