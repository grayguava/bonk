package com.grayguava.bonk.data

import android.content.Context

/** The name the user typed on the welcome screen. */
class UserStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("bonk_user", Context.MODE_PRIVATE)

    var name: String?
        get() = prefs.getString(KEY_NAME, null)?.trim()?.takeIf { it.isNotEmpty() }
        set(value) {
            val clean = value?.trim().orEmpty()
            prefs.edit()
                .apply { if (clean.isEmpty()) remove(KEY_NAME) else putString(KEY_NAME, clean) }
                .apply()
        }

    private companion object {
        const val KEY_NAME = "name"
    }
}
