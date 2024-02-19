package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ListView
import com.esperantajvortaroj.app.databinding.ActivityTranslationLanguageBinding
import com.esperantajvortaroj.app.db.DatabaseHelper
import com.esperantajvortaroj.app.db.Language


class SelectTranslationLanguageActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTranslationLanguageBinding
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslationLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appToolbar)
        supportActionBar?.title = resources.getString(R.string.title_select_translation_language)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val allLangs = DatabaseHelper(this).getLanguages()

        val listView = binding.languagesListView
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.itemsCanFocus = true

        val langPrefs = PreferenceHelper.getLanguagesPreference(this)

        val adapter = LanguageAdapter(this, allLangs, langPrefs)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, item, position, _ ->
            val lang = adapter.getItem(position) as Language
            if(item != null && item is CheckedTextView && parent is ListView){
                val langPrefs = PreferenceHelper.getLanguagesPreference(this)
                val copyLangPrefs = langPrefs.toHashSet()
                if(parent.isItemChecked(position)) {
                    copyLangPrefs.add(lang.code)
                } else {
                    copyLangPrefs.remove(lang.code)
                }
                PreferenceHelper.setLanguagesPreference(this, copyLangPrefs)
                adapter.langPrefs = copyLangPrefs
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.translation_language_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.close_translation_language){
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    class LanguageAdapter(context: Context, items: ArrayList<Language>, var langPrefs: Set<String>)
        : ArrayAdapter<Language>(context, android.R.layout.simple_list_item_multiple_choice, items){

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = super.getView(position, convertView, parent)
            if(parent is ListView && textView is CheckedTextView){
                val lang = getItem(position)
                if (lang == null) return textView
                parent.setItemChecked(position, langPrefs.contains(lang.code))

                val numEntries = String.format("%,d", lang.numEntries)
                val text = SpannableString("${lang.name}\n$numEntries kapvortoj")
                text.setSpan(ForegroundColorSpan(Color.GRAY), lang.name.length, text.length, 0)
                textView.text = text
            }
            return textView
        }
    }
}
