package com.esperantajvortaroj.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.esperantajvortaroj.app.databinding.ItemSearchHistoryEntryBinding
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryView : RelativeLayout {
    var historyEntry : SearchHistory? = null
    var word : String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun initView(context: Context, searchHistory: SearchHistory, fontSize: Float, onDelete: (View) -> Unit) {
        historyEntry = searchHistory
        this.removeAllViewsInLayout()
        val binding = ItemSearchHistoryEntryBinding.inflate(LayoutInflater.from(context), this, true)
        val view = binding.root
        binding.entryWord.text = searchHistory.word
        word = searchHistory.word
        binding.entryWord.textSize = fontSize
        view.setPadding(16, 32, 32, 16)
        binding.deleteImageView.setOnClickListener { onDelete(this) }

    }

}