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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gloster.yaplan.ui.categories.CategoriesScreen
import com.gloster.yaplan.ui.edit.TaskEditScreen
import com.gloster.yaplan.ui.edit.TaskEditViewModel
import com.gloster.yaplan.ui.list.TaskListScreen
import com.gloster.yaplan.ui.theme.YapLanTheme

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
    NavHost(navController = nav, startDestination = "list") {
        composable("list") {
            TaskListScreen(
                onAddTask = { nav.navigate("edit/${TaskEditViewModel.NEW_TASK}") },
                onOpenTask = { id -> nav.navigate("edit/$id") },
                onOpenCategories = { nav.navigate("categories") },
            )
        }
        composable(
            "edit/{${TaskEditViewModel.ARG_TASK_ID}}",
            arguments = listOf(navArgument(TaskEditViewModel.ARG_TASK_ID) { type = NavType.LongType }),
        ) {
            TaskEditScreen(onBack = { nav.popBackStack() })
        }
        composable("categories") {
            CategoriesScreen(onBack = { nav.popBackStack() })
        }
    }
}
