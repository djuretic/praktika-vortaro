package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_definition.*

class DefinitionActivity : AppCompatActivity() {
    private var entriesList: ArrayList<Int> = arrayListOf()
    private var entryPosition = 0

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definition)
        setSupportActionBar(appToolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val articleId = intent.getIntExtra(ENTRY_DATA, 0)
        val entryPosition = intent.getIntExtra(ENTRY_POSITION, 0)
        val entriesList = intent.extras.getIntegerArrayList(ENTRIES_LIST)
        this.entryPosition = entryPosition
        this.entriesList = entriesList

        val articleView = loadArticle(articleId)
        definitionScrollView.addView(articleView)
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.entry_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(menu != null){
            val prevButton = menu.findItem(R.id.prev_entry)
            val nextButton = menu.findItem(R.id.next_entry)

            prevButton.isEnabled = entryPosition != 0
            nextButton.isEnabled = entryPosition < entriesList.size - 1
            prevButton.icon.alpha = if(prevButton.isEnabled) 255 else 130
            nextButton.icon.alpha = if(nextButton.isEnabled) 255 else 130
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.prev_entry -> {
                if(entryPosition == 0){
                    return true
                }
                entryPosition--
                definitionScrollView.removeAllViews()
                definitionScrollView.addView(loadArticle(entriesList[entryPosition]))
                invalidateOptionsMenu()
                return true
            }
            R.id.next_entry -> {
                if(entryPosition >= entriesList.size - 1){
                    return true
                }
                entryPosition++
                definitionScrollView.removeAllViews()
                definitionScrollView.addView(loadArticle(entriesList[entryPosition]))
                invalidateOptionsMenu()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun loadArticle(articleId: Int): LinearLayout {
        var content : CharSequence = ""
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val databaseHelper = DatabaseHelper(this)
        val wordResult = databaseHelper.wordById(articleId)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)

        val translations = databaseHelper.translationsByWordId(articleId, langPrefs)
        val textView = TextView(this)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setTextColor(Color.BLACK)
        if(wordResult != null){
            val def = SpannableString(wordResult.definition)
            if(wordResult.format?.bold!!.isNotEmpty()){
                for(pair in wordResult.format.bold){
                    def.setSpan(StyleSpan(Typeface.BOLD), pair.first, pair.second, 0)
                }
            }
            if(wordResult.format?.italic!!.isNotEmpty()){
                for(pair in wordResult.format.italic){
                    def.setSpan(StyleSpan(Typeface.ITALIC), pair.first, pair.second, 0)
                }
            }
            val word = SpannableString(wordResult.word)
            word.setSpan(StyleSpan(Typeface.BOLD), 0, wordResult.word.length, 0)
            content = TextUtils.concat(word, "\n", def)
        }

        if(translations.isNotEmpty()){
            for(trad in translations){
                content = TextUtils.concat(content, "\n\n ", trad.lng, ": ", trad.translation)
            }
        }
        textView.text = content

        layout.addView(textView)
        return layout
    }

    companion object {
        const val ENTRY_DATA = "entry_data"
        const val ENTRY_POSITION = "entry_position"
        const val ENTRIES_LIST = "entries_list"
    }
}

