package com.gloster.yaplan.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.components.TaskRow
import com.gloster.yaplan.ui.components.YapSnackbarHost
import com.gloster.yaplan.ui.components.yapTextFieldColors
import com.gloster.yaplan.ui.edit.NewTaskSheet
import com.gloster.yaplan.ui.theme.Yap

@Composable
fun SearchScreen(
    onOpenTask: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var newTaskTitle by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

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
            Text(
                "Ara",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .padding(top = 14.dp)
                    .semantics { heading() },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .semantics { contentDescription = "Görev ara" },
                placeholder = { Text("Görev ara", color = Yap.TextFaint) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Yap.TextMuted) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Aramayı temizle", tint = Yap.TextSecondary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors = yapTextFieldColors(Yap.Surface),
            )
            Text(
                "Görev başlıklarında ve notlarda aranır.",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
                fontSize = 13.sp,
                color = Yap.TextMuted,
            )

            when {
                !state.hasQuery -> Unit
                state.results.isEmpty() -> NoResults(
                    query = state.query.trim(),
                    onClear = { viewModel.setQuery("") },
                    onAdd = {
                        // Otherwise the search field takes focus back (and the keyboard) when the sheet closes.
                        focusManager.clearFocus()
                        newTaskTitle = state.query.trim()
                    },
                )
                else -> {
                    HorizontalDivider(color = Yap.Divider)
                    Row(Modifier.height(34.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${state.results.size} sonuç bulundu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Yap.TextSecondary,
                        )
                    }
                    HorizontalDivider(color = Yap.Divider)
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(state.results, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                category = task.categoryId?.let(state.categoriesById::get),
                                now = state.now,
                                today = state.today,
                                onClick = { onOpenTask(task.id) },
                                highlight = state.query,
                            )
                        }
                    }
                }
            }
        }
    }

    newTaskTitle?.let { title ->
        NewTaskSheet(onDismiss = { newTaskTitle = null }, initialTitle = title)
    }
}

@Composable
private fun NoResults(query: String, onClear: () -> Unit, onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = Yap.EmptyIcon, modifier = Modifier.size(44.dp))
        Text("“$query” bulunamadı", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            "Başka bir kelime dene ya da bunu yeni görev olarak ekle.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Yap.TextMuted,
            textAlign = TextAlign.Center,
        )
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton("Aramayı temizle", onClick = onClear)
            PrimaryButton(
                "Görev ekle",
                onClick = onAdd,
                leading = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
        }
    }
}
