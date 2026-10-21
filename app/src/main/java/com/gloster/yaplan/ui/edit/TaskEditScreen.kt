package com.gloster.yaplan.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gloster.yaplan.ui.components.LocalAppSnackbar
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.components.TaskCheckbox
import com.gloster.yaplan.ui.components.YapSnackbarHost
import com.gloster.yaplan.ui.formatDateTime
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.widget.requestTaskWidget
import kotlinx.coroutines.launch

@Composable
fun TaskEditScreen(
    onBack: () -> Unit,
    viewModel: TaskEditViewModel = viewModel(factory = TaskEditViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbar = LocalAppSnackbar.current
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Yap.Background)
                .imePadding(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text(
                    "Görevi düzenle",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = Yap.Divider)

            if (!state.loaded) return@Column

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                DoneToggle(done = state.isDone, onChange = viewModel::setDone)
                TaskForm(state = state, categories = categories, actions = viewModel, fieldColor = Yap.Surface)
                SecondaryButton(
                    "Widget olarak ekle",
                    onClick = {
                        if (!requestTaskWidget(context, viewModel.taskId)) {
                            scope.launch {
                                snackbar.showSnackbar("Ana ekrana uzun bas → Widget'lar → YAP LAN! yolunu kullan.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leading = { Icon(Icons.Outlined.Widgets, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
                SecondaryButton(
                    "Görevi sil",
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = Yap.Danger,
                    borderColor = Yap.DangerBorder,
                    leading = { Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
                state.createdAt?.let {
                    Text(
                        "${formatDateTime(it)}'de eklendi",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Yap.TextFaint,
                    )
                }
            }

            HorizontalDivider(color = Yap.Border)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Yap.Surface)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
            ) {
                PrimaryButton(
                    "Kaydet",
                    onClick = { viewModel.save(onBack) },
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        YapSnackbarHost(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Yap.DialogSurface,
            title = { Text("Görev silinsin mi?") },
            text = {
                Text(
                    "“${state.title.trim()}” silinecek. Silmeden hemen sonra “Geri al” ile geri getirebilirsin.",
                    color = Yap.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onBack)
                }) { Text("Sil", color = Yap.Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Vazgeç", color = Yap.Text) } },
        )
    }
}

@Composable
private fun DoneToggle(done: Boolean, onChange: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(if (done) Yap.AccentContainer else Yap.Surface, shape)
            .border(1.dp, if (done) Yap.Accent else Yap.Border, shape)
            .toggleable(value = done, role = Role.Checkbox, onValueChange = onChange)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TaskCheckbox(checked = done, overdue = false)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (done) "Tamamlandı" else "Henüz yapılmadı",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (done) "Yapılacaklara geri almak için dokun" else "Bitirdiysen işaretle",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Yap.TextMuted,
            )
        }
    }
}
