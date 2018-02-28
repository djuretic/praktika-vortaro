package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.BaseAdapter
import android.support.v7.widget.SearchView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.text.util.Linkify
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class SearchActivity : AppCompatActivity() {
    private var searchAdapter : SearchResultAdapter? = null
    private val ESPERANTO = "eo"
    private val ACTIVE_LANGUAGE = "active_language"
    private var activeLanguage = ESPERANTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)

        searchAdapter = SearchResultAdapter(this)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                if(query == null) return true
                var text = query.trim()
                if(activeLanguage == ESPERANTO){
                    text = Utils.addHats(text)
                }
                searchAdapter?.filter(text, activeLanguage)
                return true
            }

            override fun onQueryTextSubmit(p0: String?) = true
        })
        searchResults.adapter = searchAdapter

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        activeLanguage = sharedPref.getString(ACTIVE_LANGUAGE, ESPERANTO)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if(menu != null){
            val langButton = menu?.findItem(R.id.change_search_language)
            langButton.title = activeLanguage
        }
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
                else {
                    activeLanguage = ESPERANTO
                }

                val edit = sharedPref.edit()
                edit.putString(ACTIVE_LANGUAGE, activeLanguage)
                edit.apply()

                item.title = activeLanguage
                val originalQuery = searchView.query
                searchView.setQuery("", true)
                searchView.setQuery(originalQuery, true)
                return true
            }
            R.id.about_the_app -> {
                showAboutDialog()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun showAboutDialog(){
        val builder = AlertDialog.Builder(this)
        val title = resources.getString(R.string.app_name)
        val message = SpannableString("""@ 2018 Dušan Juretić
            |
            |Datumbazo: Reta Vortaro - http://www.reta-vortaro.de/revo
            |
            |Inspirita de Prevo - https://play.google.com/store/apps/details?id=uk.co.busydoingnothing.prevo
        """.trimMargin())
        Linkify.addLinks(message, Linkify.ALL)

        val dialog = builder.setMessage(message).setTitle("Pri $title")
                .setPositiveButton(R.string.close_dialog, null)
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message).movementMethod = LinkMovementMethod.getInstance()
    }

    private class SearchResultAdapter(val context: Context): BaseAdapter() {
        private var results = ArrayList<SearchResult>()
        private val databaseHelper = DatabaseHelper(context)

         fun filter(searchString: String, language: String){
            if(databaseHelper == null)
                return
            if(searchString == ""){
                results.clear()
            } else {
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
                    if(entryId > 0) {
                        randomIntent.putExtra(DefinitionActivity.ENTRY_DATA, entryId)
                        randomIntent.putExtra(DefinitionActivity.ENTRY_POSITION, position)

                        val bundle = Bundle()
                        bundle.putIntegerArrayList(
                                DefinitionActivity.ENTRIES_LIST,
                                ArrayList(results.map { x -> x.id }))
                        randomIntent.putExtras(bundle)
                        context.startActivity(randomIntent)
                    }

                }
            })

            return resultRow
        }
    }
}

