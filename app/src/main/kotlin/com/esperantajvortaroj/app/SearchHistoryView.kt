package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.esperantajvortaroj.app.databinding.ItemSearchHistoryEntryBinding
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryView : RelativeLayout {
    var historyEntry : SearchHistory? = null
    var word : String? = null
    private var binding: ItemSearchHistoryEntryBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemSearchHistoryEntryBinding.inflate(inflater, this, true)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun initView(context: Context, searchHistory: SearchHistory, fontSize: Float, onDelete: (View) -> Unit) {
        historyEntry = searchHistory
        this.removeAllViewsInLayout()
        this.addView(binding.root)
        binding.entryWord.text = searchHistory.word
        word = searchHistory.word
        binding.entryWord.textSize = fontSize
        binding.root.setPadding(16, 32, 32, 16)
        binding.deleteImageView.setOnClickListener { onDelete(this) }

    }

}