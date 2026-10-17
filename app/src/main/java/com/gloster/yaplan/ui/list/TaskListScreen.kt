package com.gloster.yaplan.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.data.Category
import com.gloster.yaplan.data.Task
import com.gloster.yaplan.ui.formatDateTime
import com.gloster.yaplan.ui.theme.color
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenCategories: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun deleteWithUndo(task: Task) {
        viewModel.delete(task)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = "\"${task.title}\" silindi",
                actionLabel = "Geri al",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restore(task)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YAP LAN!", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Kategoriler")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Görev ekle")
            }
        },
        snackbarHost = { SnackbarHost(snackbar, Modifier.imePadding()) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Görev ara") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
            )

            PrimaryTabRow(
                selectedTabIndex = state.filter.tab.ordinal,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                TaskTab.entries.forEach { tab ->
                    Tab(
                        selected = state.filter.tab == tab,
                        onClick = { viewModel.setTab(tab) },
                        text = { Text(tab.label) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.filter.categoryId == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("Hepsi") },
                        )
                    }
                    items(state.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = state.filter.categoryId == category.id,
                            onClick = { viewModel.setCategory(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = { ColorDot(Color(category.color)) },
                        )
                    }
                }
            }

            if (state.tasks.isEmpty()) {
                EmptyState(Modifier.weight(1f))
            } else {
                val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        SwipeToDelete(onDelete = { deleteWithUndo(task) }, modifier = Modifier.animateItem()) {
                            TaskRow(
                                task = task,
                                category = task.categoryId?.let(categoriesById::get),
                                onToggle = { viewModel.toggleDone(task) },
                                onClick = { onOpenTask(task.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeToDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = { onDelete() },
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                },
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White)
            }
        },
    ) {
        content()
    }
}

@Composable
private fun TaskRow(
    task: Task,
    category: Category?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    val overdue = !task.isDone && task.dueAt != null && task.dueAt < System.currentTimeMillis()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(task.priority.color),
            )
            Row(
                Modifier.padding(vertical = 10.dp).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
                Column(Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                        color = if (task.isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    if (task.note.isNotBlank()) {
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (category != null || task.dueAt != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (category != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ColorDot(Color(category.color))
                                    Spacer(Modifier.width(4.dp))
                                    Text(category.name, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            if (task.dueAt != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (task.remind) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Hatırlatma açık",
                                            modifier = Modifier.size(14.dp),
                                            tint = if (overdue) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        )
                                        Spacer(Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = formatDateTime(task.dueAt),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (overdue) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorDot(color: Color, size: Int = 10) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Burada görev yok", style = MaterialTheme.typography.titleMedium)
            Text(
                "Eklemek için + butonuna bas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
