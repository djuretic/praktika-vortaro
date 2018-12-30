package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class SearchHistoryAdapter(val context: Context) : BaseAdapter() {
    private var items = emptyList<SearchHistory>()

    fun receiveDataSet(newItems: List<SearchHistory>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val fontSize = sharedPrefs.getInt(SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE)
        val resultRow = convertView ?: TextView(context)
        if (resultRow is TextView) {
            val historyEntry = getItem(position)
            resultRow.text = historyEntry.word
            resultRow.textSize = fontSize.toFloat()
            resultRow.setTextColor(Color.BLACK)
            resultRow.setPadding(16, 8, 16, 8)
        }
        return resultRow
    }

    override fun getItem(position: Int) = items[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getCount() = items.size

}