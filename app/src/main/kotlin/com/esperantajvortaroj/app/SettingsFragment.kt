package com.esperantajvortaroj.app

import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import com.esperantajvortaroj.app.db.DatabaseHelper

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        val langPref = findPreference<MultiSelectListPreference>(SettingsActivity.KEY_LANGUAGES_PREFERENCE)
        val appContext = context
        if(langPref != null && appContext != null){
            val databaseHelper = DatabaseHelper(appContext)
            val allLangs = databaseHelper.getLanguages()
            val langNames = allLangs.map { lng -> lng.name }
            val langCodes = allLangs.map { lng -> lng.code }

            langPref.entryValues = langCodes.toTypedArray()
            langPref.entries = langNames.toTypedArray()
        }
    }
}
