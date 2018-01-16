package com.esperantajvortaroj.app

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_definition.*

class DefinitionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definition)

        val articleId = intent.getIntExtra(ENTRY_DATA, 0)
        val articleView = loadArticle(articleId)
        definitionScrollView.addView(articleView)
    }

    private fun loadArticle(articleId: Int): LinearLayout {
        var content : CharSequence = ""
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val databaseHelper = DatabaseHelper(this)
        val wordResult = databaseHelper.wordById(articleId)
        val translations = databaseHelper.translationsByWordId(articleId)
        val textView = TextView(this)
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
    }
}

