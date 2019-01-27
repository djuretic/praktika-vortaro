package com.esperantajvortaroj.app

import android.content.Context
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryAdapter(val context: Context) : BaseAdapter() {
    private var items = emptyList<SearchHistory>()

    fun receiveDataSet(newItems: List<SearchHistory>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val fontSize = sharedPrefs.getInt(SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE)
        val historyEntry = getItem(position)
        val resultRow = convertView ?: SearchHistoryView(context)
        if (resultRow is SearchHistoryView) {
            resultRow.initView(context, historyEntry, fontSize.toFloat())
        }
        return resultRow
    }

    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getCount() = items.size

}