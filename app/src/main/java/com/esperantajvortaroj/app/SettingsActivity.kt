package com.esperantajvortaroj.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val KEY_LANGUAGES_PREFERENCE = "languages_preference"
        const val VERSION_CODE = "version_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}
