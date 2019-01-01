package com.esperantajvortaroj.app

import android.os.Bundle
import android.preference.MultiSelectListPreference
import android.preference.PreferenceFragment
import com.esperantajvortaroj.app.db.DatabaseHelper

class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        val langPref = findPreference(SettingsActivity.KEY_LANGUAGES_PREFERENCE)
        if(langPref is MultiSelectListPreference){
            val databaseHelper = DatabaseHelper(activity)
            val allLangs = databaseHelper.getLanguages()
            val langNames = allLangs.map { lng -> lng.name }
            val langCodes = allLangs.map { lng -> lng.code }

            langPref.entryValues = langCodes.toTypedArray()
            langPref.entries = langNames.toTypedArray()
        }
    }
}
