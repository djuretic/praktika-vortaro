package com.esperantajvortaroj.app

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.BaseAdapter
import android.support.v7.widget.SearchView
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class SearchActivity : AppCompatActivity() {
    private var searchAdapter = SearchResultAdapter(this)
    private val ESPERANTO = "eo"
    private var activeLanguage = ESPERANTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                if(query == null) return true
                searchAdapter.filter(Utils.addHats(query.trim()), activeLanguage)
                return true
            }

            override fun onQueryTextSubmit(p0: String?) = true
        })
        searchResults.adapter = searchAdapter

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
                return true
            }
            R.id.change_search_language -> {
                val ESPERANTO = "eo"
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
                val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)

                if(activeLanguage == ESPERANTO && !langPrefs.isEmpty()){
                    activeLanguage = langPrefs.elementAt(0)
                } else if (activeLanguage != ESPERANTO){
                    val currentIndex = langPrefs.indexOf(activeLanguage)
                    if(currentIndex < 0 || currentIndex >= langPrefs.size - 1){
                        activeLanguage = ESPERANTO
                    } else {
                        activeLanguage = langPrefs.elementAt(currentIndex+1)
                    }
                }
                else activeLanguage = ESPERANTO
                item.title = activeLanguage
                val originalQuery = searchView.query
                searchView.setQuery("", true)
                searchView.setQuery(originalQuery, true)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private class SearchResultAdapter(context: Context): BaseAdapter() {
        private val context: Context = context
        private var results = ArrayList<SearchResult>()

        fun filter(searchString: String, language: String){
            if(searchString == ""){
                results.clear()
            } else {
                val databaseHelper = DatabaseHelper(context)
                //TODO not on main thread
                if(language == "eo"){
                    results = databaseHelper.searchWords(searchString)
                } else {
                    results = databaseHelper.searchTranslations(searchString, language)
                }
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
                entryId = foundEntry.id

                val def = SpannableString(foundEntry.definition)
                if(foundEntry.format?.bold != null){
                    for(pair in foundEntry.format.bold){
                        def.setSpan(StyleSpan(Typeface.BOLD), pair.first, pair.second, 0)
                    }
                }
                if(foundEntry.format?.italic != null){
                    for(pair in foundEntry.format.italic){
                        def.setSpan(StyleSpan(Typeface.ITALIC), pair.first, pair.second, 0)
                    }
                }
                definition.text = def
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

