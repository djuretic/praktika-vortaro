package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_definition.*

class DefinitionActivity : AppCompatActivity() {
    private var entriesList: ArrayList<Int> = arrayListOf()
    private var entryPosition = 0
    private var wordId = 0
    private var articleId = 0
    private var showArticle = false

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definition)
        setSupportActionBar(appToolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        wordId = intent.getIntExtra(WORD_ID, 0)
        articleId = intent.getIntExtra(ARTICLE_ID, 0)
        val entryPosition = intent.getIntExtra(ENTRY_POSITION, 0)
        val entriesList = intent.extras.getIntegerArrayList(ENTRIES_LIST)
        this.entryPosition = entryPosition
        this.entriesList = entriesList

        displayArticleAndWord(wordId)
        invalidateOptionsMenu()
    }

    private fun displayArticleAndWord(wordId: Int) {
        val wordInfo = loadWord(wordId)
        val wordView = wordInfo.first
        val articleId = wordInfo.second
        val articleViews = loadArticle(articleId, wordId)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(wordView)
        if(articleViews.isNotEmpty()){
            layout.addView(articleHeader(wordId))
            if(showArticle){
                layout.addView(articleSeparator())
                for(view in articleViews){
                    layout.addView(view)
                }
            }
        }

        with(definitionScrollView){
            removeAllViews()
            addView(layout)
        }
    }

    private fun articleSeparator(): View {
        val view = View(this)
        view.minimumHeight = 1
        view.setBackgroundColor(Color.GRAY)
        return view
    }

    private fun articleHeader(wordId: Int): TextView {
        val textView = TextView(this)
        val text: SpannableString
        if(showArticle)
            text = SpannableString("\nKaŝi artikolon\n")
        else
            text = SpannableString("\nMontri artikolon\n")
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        textView.text = text
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setOnClickListener {
            showArticle = !showArticle
            displayArticleAndWord(wordId)
        }
        return textView
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
                displayArticleAndWord(entriesList[entryPosition])
                invalidateOptionsMenu()
                return true
            }
            R.id.next_entry -> {
                if(entryPosition >= entriesList.size - 1){
                    return true
                }
                entryPosition++
                displayArticleAndWord(entriesList[entryPosition])
                invalidateOptionsMenu()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun loadWord(wordId: Int): Pair<LinearLayout, Int> {
        val databaseHelper = DatabaseHelper(this)
        val wordResult = databaseHelper.wordById(wordId)

        val pair = getTextViewOfWord(wordResult)
        val textView = pair.first
        var content = pair.second

        content = addTranslations(databaseHelper, wordId, content)
        textView.text = content

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(textView)
        return Pair(layout, wordResult?.articleId ?: 0)
    }

    private fun loadArticle(articleId: Int, wordId: Int): List<View> {
        val databaseHelper = DatabaseHelper(this)
        val results = databaseHelper.articleById(articleId)

        if (results.size == 1){
            return emptyList()
        }

        return results.map { res ->
            val pair = getTextViewOfWord(res)
            val textView = pair.first
            var content = pair.second

            content = addTranslations(databaseHelper, res.id, content)
            textView.text = TextUtils.concat(content, "\n")
            if(res.id == wordId) textView.setBackgroundColor(Color.parseColor("#dddddd"))
            textView
        }
    }

    private fun addTranslations(databaseHelper: DatabaseHelper, wordId: Int, content: CharSequence): CharSequence {
        var content1 = content
        val translationsByLang = getTranslations(databaseHelper, wordId)
        val langNames = databaseHelper.getLanguagesHash()
        if (translationsByLang.isNotEmpty()) {
            val translationsTitle = SpannableString("\n\nTradukoj")
            translationsTitle.setSpan(UnderlineSpan(), 0, translationsTitle.length, 0)
            content1 = TextUtils.concat(content1, translationsTitle)
            for (langEntry in langNames) {
                val translations = translationsByLang.get(langEntry.key)
                if (translations != null) {
                    val lang = langEntry.value
                    content1 = TextUtils.concat(
                            content1,
                            "\n\n• ", lang, "j: ",
                            translations.joinToString(", ") { it.translation })
                }

            }
        }
        return content1
    }

    private fun getTextViewOfWord(wordResult: SearchResult?): Pair<TextView, CharSequence> {
        var content : CharSequence = ""
        val textView = TextView(this)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setTextColor(Color.BLACK)
        if (wordResult != null) {
            val word = SpannableString(wordResult.word)
            word.setSpan(StyleSpan(Typeface.BOLD), 0, wordResult.word.length, 0)
            content = TextUtils.concat(word, "\n", wordResult.formattedDefinition())
        }
        return Pair(textView, content)
    }

    private fun getTranslations(databaseHelper: DatabaseHelper, wordId: Int): LinkedHashMap<String, List<TranslationResult>> {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)

        val translationsByLang = LinkedHashMap<String, List<TranslationResult>>()
        for (lang in langPrefs) {
            val translations = databaseHelper.translationsByWordId(wordId, lang)
            if (translations.isNotEmpty()) {
                translationsByLang.put(lang, translations)
            }
        }
        return translationsByLang
    }

    companion object {
        const val WORD_ID = "word_id"
        const val ARTICLE_ID = "article_id"
        const val ENTRY_POSITION = "entry_position"
        const val ENTRIES_LIST = "entries_list"
    }
}

