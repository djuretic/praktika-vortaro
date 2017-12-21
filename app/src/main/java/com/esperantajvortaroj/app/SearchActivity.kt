package com.esperantajvortaroj.app

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.support.v7.widget.SearchView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class SearchActivity : AppCompatActivity() {
    private var searchAdapter = SearchResultAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                if(query == null) return true
                searchAdapter.filter(query.trim())
                return true
            }

            override fun onQueryTextSubmit(p0: String?) = true
        })
        searchResults.adapter = searchAdapter
    }

    private class SearchResultAdapter(context: Context): BaseAdapter() {
        private val context: Context = context
        private var results = ArrayList<SearchResult>()

        fun filter(searchString: String){
            if(searchString == ""){
                results.clear()
            } else {
                val databaseHelper = DatabaseHelper(context)
                //TODO not on main thread
                results = databaseHelper.searchWords(searchString)
            }
            if(results.count() > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
        }

        override fun getCount() = results.size

        override fun getItem(position: Int): Any {
            val res = results[position]
            if(res != null) return res
            return "null"
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(context)
            val resultRow = if (convertView == null)
                layoutInflater.inflate(R.layout.search_entry, parent, false)
            else { convertView }

            val mainWord = resultRow.findViewById<TextView>(R.id.mainWord)
            val definition = resultRow.findViewById<TextView>(R.id.definition)
            val foundEntry = getItem(position)
            var entryId = 0
            if(foundEntry is SearchResult){
                mainWord.text = foundEntry.word
                definition.text = foundEntry.definition
                entryId = foundEntry.id
            }

            resultRow.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val randomIntent = Intent(context, DefinitionActivity::class.java)
                    if(entryId > 0) randomIntent.putExtra(DefinitionActivity.ENTRY_DATA, entryId)
                    context.startActivity(randomIntent)
                }
            })

            return resultRow
        }
    }
}

