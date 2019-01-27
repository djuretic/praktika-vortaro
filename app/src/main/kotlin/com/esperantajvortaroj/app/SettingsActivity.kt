package com.esperantajvortaroj.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    companion object {
        /* Show Revo's translations in these languages */
        const val KEY_LANGUAGES_PREFERENCE = "languages_preference"
        const val ACTIVE_LANGUAGE = "active_language"
        const val VERSION_CODE = "version_code"
        const val FONT_SIZE = "font_size"
        const val DEFAULT_FONT_SIZE = 18
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}
