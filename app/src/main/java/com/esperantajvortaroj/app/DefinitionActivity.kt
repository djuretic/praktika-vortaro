package com.esperantajvortaroj.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val databaseHelper = DatabaseHelper(this)
        val wordResult = databaseHelper.wordById(articleId)
        val textView = TextView(this)
        if(wordResult != null){
            textView.text = wordResult.word + "\n" + wordResult.definition
        }
        layout.addView(textView)
        return layout
    }

    companion object {
        const val ENTRY_DATA = "entry_data"
    }
}

