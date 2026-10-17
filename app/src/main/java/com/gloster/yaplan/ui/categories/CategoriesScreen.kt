package com.gloster.yaplan.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.CategoryColors
import com.gloster.yaplan.ui.list.ColorDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategoriler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = Category(name = "", color = CategoryColors[categories.size % CategoryColors.size])
            }) {
                Icon(Icons.Default.Add, contentDescription = "Kategori ekle")
            }
        },
    ) { padding ->
        if (categories.isEmpty()) {
            Text(
                "Henüz kategori yok",
                modifier = Modifier.padding(padding).padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 88.dp)) {
            items(categories, key = { it.id }) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    leadingContent = { ColorDot(Color(category.color), size = 16) },
                    trailingContent = {
                        IconButton(onClick = { deleting = category }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    },
                    modifier = Modifier.clickable { editing = category },
                )
            }
        }
    }

    editing?.let { category ->
        CategoryDialog(
            initial = category,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            },
        )
    }

    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("\"${category.name}\" silinsin mi?") },
            text = { Text("Bu kategorideki görevler silinmez, kategorisiz kalır.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(category)
                    deleting = null
                }) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun CategoryDialog(initial: Category, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var color by remember { mutableStateOf(initial.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "Yeni kategori" else "Kategoriyi düzenle") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CategoryColors.forEach { c ->
                        val selected = c == color
                        ColorSwatch(Color(c), selected) { color = c }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(initial.copy(name = name, color = color)) },
                enabled = name.isNotBlank(),
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } },
    )
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val ring = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        Modifier
            .size(36.dp)
            .border(3.dp, ring, CircleShape)
            .padding(5.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
    )
}
