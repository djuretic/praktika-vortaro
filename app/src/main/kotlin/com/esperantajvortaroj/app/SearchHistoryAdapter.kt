package com.esperantajvortaroj.app

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryAdapter(val context: Context) : BaseAdapter() {
    private var items = emptyList<SearchHistory>()
    private var onDelete: (View)-> Unit = {}

    fun receiveDataSet(newItems: List<SearchHistory>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setOnDelete (handler: (View) -> Unit) {
        onDelete = handler
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val fontSize = PreferenceHelper.getFontSize(context)
        val historyEntry = getItem(position)
        val resultRow = convertView ?: SearchHistoryView(context)
        if (resultRow is SearchHistoryView) {
            resultRow.initView(context, historyEntry, fontSize.toFloat(), onDelete)
        }
        return resultRow
    }

    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getCount() = items.size

}