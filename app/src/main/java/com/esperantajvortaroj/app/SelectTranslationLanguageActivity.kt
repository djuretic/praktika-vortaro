package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_translation_language.*


class SelectTranslationLanguageActivity : AppCompatActivity() {
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translation_language)
        setSupportActionBar(appToolbar)
        supportActionBar?.title = resources.getString(R.string.item_select_translation_language)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val allLangs = DatabaseHelper(this).getLanguages()

        val listView = languagesListView
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.itemsCanFocus = true

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)

        val adapter = LanguageAdapter(this, allLangs, langPrefs)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, item, position, id ->
            val lang = adapter.getItem(position) as Language
            if(item != null && item is CheckedTextView && parent is ListView){
                val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)
                val copyLangPrefs = langPrefs.toHashSet()
                if(parent.isItemChecked(position)) {
                    copyLangPrefs.add(lang.code)
                } else {
                    copyLangPrefs.remove(lang.code)
                }
                val editor = sharedPref.edit()
                editor.putStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, copyLangPrefs)
                editor.apply()
                adapter.langPrefs = copyLangPrefs
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class LanguageAdapter(context: Context, items: ArrayList<Language>, var langPrefs: MutableSet<String>)
        : ArrayAdapter<Language>(context, android.R.layout.simple_list_item_multiple_choice, items){

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView = super.getView(position, convertView, parent)
            if(parent is ListView && textView is CheckedTextView){
                val lang = getItem(position)
                parent.setItemChecked(position, langPrefs.contains(lang.code))
                textView.text = lang.name
            }
            return textView
        }
    }
}
