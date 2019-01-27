package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.esperantajvortaroj.app.db.SearchHistory
import kotlinx.android.synthetic.main.item_search_history_entry.view.*

class SearchHistoryView : RelativeLayout {
    var historyEntry : SearchHistory? = null
    var word : String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun initView(context: Context, searchHistory: SearchHistory, fontSize: Float, onDelete: (View) -> Unit) {
        historyEntry = searchHistory
        this.removeAllViewsInLayout()
        val view = LayoutInflater.from(context).inflate(R.layout.item_search_history_entry, this, true)
        view.entryWord.text = searchHistory.word
        word = searchHistory.word
        view.entryWord.textSize = fontSize
        view.entryWord.setTextColor(Color.BLACK)
        view.setPadding(16, 32, 32, 16)
        view.deleteImageView.setOnClickListener { onDelete(this) }

    }

}