package com.gloster.yaplan.data

import android.content.Context
import androidx.core.content.edit

/** Small per-device flags that don't belong in the database. */
class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("yaplan", Context.MODE_PRIVATE)

    fun isPromoDismissed(key: String): Boolean = prefs.getBoolean("promo_dismissed_$key", false)

    fun dismissPromo(key: String) = prefs.edit { putBoolean("promo_dismissed_$key", true) }
}
