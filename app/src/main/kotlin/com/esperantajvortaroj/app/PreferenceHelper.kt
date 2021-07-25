package com.esperantajvortaroj.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferenceHelper {
    private fun defaultSharedPreferences(context: Context): SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(context)

    private fun getInt(context: Context, key: String, default: Int): Int {
        return defaultSharedPreferences(context).getInt(key, default)
    }

    private fun getString(context: Context, key: String, default: String): String{
        return defaultSharedPreferences(context).getString(key, default)
    }

    private fun getStringSet(context: Context, key: String): Set<String> {
        return defaultSharedPreferences(context).getStringSet(key, emptySet())
    }

    private fun putInt(context: Context, key: String, value: Int) {
        val edit = defaultSharedPreferences(context).edit()
        edit.putInt(key, value)
        edit.apply()
    }

    private fun putString(context: Context, key: String, value: String){
        val edit = defaultSharedPreferences(context).edit()
        edit.putString(key, value)
        edit.apply()
    }

    private fun putStringSet(context: Context, key: String, value: HashSet<String>) {
        val edit = defaultSharedPreferences(context).edit()
        edit.putStringSet(key, value)
        edit.apply()
    }

    fun setVersionCode(context: Context, value: Int) {
        putInt(context, SettingsActivity.VERSION_CODE, value)
    }

    fun getVersionCode(context: Context): Int {
        return getInt(context, SettingsActivity.VERSION_CODE, 0)
    }

    fun setActiveLanguage(context: Context, value: String) {
        putString(context, SettingsActivity.ACTIVE_LANGUAGE, value)
    }

    fun getActiveLanguage(context: Context, default: String): String {
        return getString(context, SettingsActivity.ACTIVE_LANGUAGE, default)
    }

    fun setLanguagesPreference(context: Context, value: HashSet<String>) {
        putStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE, value)
    }

    fun getLanguagesPreference(context: Context): Set<String> {
        return getStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE)
    }

    fun setFontSize(context: Context, value: Int) {
        putInt(context, SettingsActivity.FONT_SIZE, value)
    }

    fun getFontSize(context: Context): Int {
        return getInt(context, SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE)
    }
}