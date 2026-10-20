package com.gloster.yaplan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/** Links the widget and notifications use to open a screen; Navigation matches them by pattern. */
object DeepLinks {
    const val HABITS = "yaplan://habits"
    const val HABIT = "yaplan://habit/{habitId}"

    fun habit(id: Long): Uri = "yaplan://habit/$id".toUri()

    /** A fresh task, so Navigation builds the back stack under the linked screen. */
    fun intent(context: Context, uri: Uri): Intent =
        Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}
