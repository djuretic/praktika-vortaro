package com.esperantajvortaroj.app

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryTextView : TextView {
    var historyEntry : SearchHistory? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun setItem(searchHistory: SearchHistory) {
        historyEntry = searchHistory
        text = searchHistory.word
    }
}