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
}