package com.gloster.yaplan

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gloster.yaplan.ui.AppBottomBar
import com.gloster.yaplan.ui.TopLevelDestination
import com.gloster.yaplan.ui.categories.CategoriesScreen
import com.gloster.yaplan.ui.components.LocalAppSnackbar
import com.gloster.yaplan.ui.edit.TaskEditScreen
import com.gloster.yaplan.ui.edit.TaskEditViewModel
import com.gloster.yaplan.ui.list.TaskListScreen
import com.gloster.yaplan.ui.search.SearchScreen
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.theme.YapLanTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            YapLanTheme {
                RequestNotificationPermission()
                AppNavigation()
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

@Composable
private fun AppNavigation() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val current = TopLevelDestination.entries.find { it.route == backStackEntry?.destination?.route }
    val snackbar = remember { SnackbarHostState() }
    val repository = (LocalContext.current.applicationContext as YapLanApp).repository
    val scope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        repository.deletedTasks.collectLatest { task ->
            val result = snackbar.showSnackbar(
                message = "“${task.title}” silindi",
                actionLabel = "Geri al",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) scope.launch { repository.saveTask(task) }
        }
    }

    val openTask: (Long) -> Unit = { id -> nav.navigate("edit/$id") }

    CompositionLocalProvider(LocalAppSnackbar provides snackbar) {
        Scaffold(
            containerColor = Yap.Background,
            bottomBar = {
                if (current != null) {
                    AppBottomBar(current) { dest ->
                        nav.navigate(dest.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = TopLevelDestination.TASKS.route,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            ) {
                composable(TopLevelDestination.TASKS.route) { TaskListScreen(onOpenTask = openTask) }
                composable(TopLevelDestination.SEARCH.route) { SearchScreen(onOpenTask = openTask) }
                composable(TopLevelDestination.CATEGORIES.route) { CategoriesScreen() }
                composable(
                    "edit/{${TaskEditViewModel.ARG_TASK_ID}}",
                    arguments = listOf(navArgument(TaskEditViewModel.ARG_TASK_ID) { type = NavType.LongType }),
                ) {
                    TaskEditScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
