package com.gloster.yaplan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/** Links the widget and notifications use to open a screen; Navigation matches them by pattern. */
object DeepLinks {
    const val HABITS = "yaplan://habits"
    const val HABIT = "yaplan://habit/{habitId}"
    const val TASK = "yaplan://task/{taskId}"
    const val ARG_NEW_TASK = "new"
    const val TASKS = "yaplan://tasks?$ARG_NEW_TASK={$ARG_NEW_TASK}"

    fun habit(id: Long): Uri = "yaplan://habit/$id".toUri()

    fun task(id: Long): Uri = "yaplan://task/$id".toUri()

    val tasks: Uri = "yaplan://tasks".toUri()

    /** Görevler with the "Yeni görev" sheet already open. */
    val newTask: Uri = "yaplan://tasks?$ARG_NEW_TASK=true".toUri()

    /** A fresh task, so Navigation builds the back stack under the linked screen. */
    fun intent(context: Context, uri: Uri): Intent =
        Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}
