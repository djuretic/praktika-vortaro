package com.esperantajvortaroj.app

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceHelper {
    fun defaultSharedPreferences(context: Context): SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(context)

    fun getString(context: Context, key: String, default: String): String{
        return defaultSharedPreferences(context).getString(key, default)
    }

    fun getStringSet(context: Context, key: String): Set<String> {
        return defaultSharedPreferences(context).getStringSet(key, emptySet())
    }

    fun putString(context: Context, key: String, value: String){
        val edit = defaultSharedPreferences(context).edit()
        edit.putString(key, value)
        edit.apply()
    }

    fun putStringSet(context: Context, key: String, value: HashSet<String>) {
        val edit = defaultSharedPreferences(context).edit()
        edit.putStringSet(key, value)
        edit.apply()
    }

    fun setActiveLanguage(context: Context, activeLanguage: String) {
        putString(context, SettingsActivity.ACTIVE_LANGUAGE, activeLanguage)
    }

    fun getActiveLanguage(context: Context, default: String): String {
        return getString(context, SettingsActivity.ACTIVE_LANGUAGE, default)
    }

    fun setLanguagesPreference(context: Context, langPrefs: HashSet<String>) {
        putStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE, langPrefs)
    }

    fun getLanguagesPreference(context: Context): Set<String> {
        return getStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE)
    }
}