package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*


class SearchActivity : AppCompatActivity() {
    private var searchAdapter : SearchResultAdapter? = null
    private val ESPERANTO = "eo"
    private val ACTIVE_LANGUAGE = "active_language"
    private var activeLanguage = ESPERANTO
    private var searchView: SearchView? = null
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)

        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        searchAdapter = SearchResultAdapter(this)
        searchResults.adapter = searchAdapter

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        activeLanguage = sharedPref.getString(ACTIVE_LANGUAGE, ESPERANTO)

        versionChecks(sharedPref)
    }

    private fun versionChecks(sharedPref: SharedPreferences) {
        val versionCode = sharedPref.getInt(SettingsActivity.VERSION_CODE, 0)
        if (versionCode == 0){
            // first run
            startLanguageActivity()
        }
        if (versionCode != BuildConfig.VERSION_CODE) {
            with(sharedPref.edit()) {
                putInt(SettingsActivity.VERSION_CODE, BuildConfig.VERSION_CODE)
                apply()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if(menu != null){
            val langButton = menu.findItem(R.id.change_search_language)
            langButton.title = activeLanguage

            val searchItem = menu.findItem(R.id.app_bar_search)
            val searchView = searchItem.actionView as SearchView

            searchView.setIconifiedByDefault(false)

            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String?): Boolean {
                    if(query == null || query.length == 0) {
                        isSearching = false
                        updateBottomPart(false, 0)
                        return true
                    }

                    var text = query.trim()
                    if(activeLanguage == ESPERANTO){
                        text = Utils.addHats(text)
                    }
                    searchAdapter?.filter(text, activeLanguage)
                    isSearching = true
                    updateBottomPart(true, searchAdapter?.count ?: 0)
                    return true
                }

                override fun onQueryTextSubmit(p0: String?) = true
            })
            this.searchView = searchView
            updateSearchQueryHint()

            searchItem.expandActionView()
            searchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    // avoid collapse of the searchView
                    finish()
                    return false
                }

                override fun onMenuItemActionExpand(p0: MenuItem?) = true
            })
        }
        return true
    }

    private fun updateBottomPart(enteredText: Boolean, resultsCount: Int) {
        if(isSearching){
            progressBarSearch.visibility = View.VISIBLE
            noResultsFound.visibility = View.GONE
            searchResults.visibility = View.GONE
            return
        }
        progressBarSearch.visibility = View.GONE
        if(!enteredText){
            noResultsFound.visibility = View.GONE
            searchResults.visibility = View.GONE
        } else if(resultsCount == 0){
            noResultsFound.visibility = View.VISIBLE
            searchResults.visibility = View.GONE
        } else {
            noResultsFound.visibility = View.GONE
            searchResults.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
                return true
            }
            R.id.change_search_language -> {
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
                updateSearchQueryHint()

                val edit = sharedPref.edit()
                edit.putString(ACTIVE_LANGUAGE, activeLanguage)
                edit.apply()

                item.title = activeLanguage
                val searchView = this.searchView
                if(searchView != null){
                    val originalQuery = searchView.query
                    searchView.setQuery("", true)
                    searchView.setQuery(originalQuery, true)
                }
                return true
            }
            R.id.select_translation_language -> {
                startLanguageActivity()
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

    private fun startLanguageActivity() {
        val intent = Intent(this, SelectTranslationLanguageActivity::class.java)
        startActivity(intent)
    }

    private fun updateSearchQueryHint() {
        if (activeLanguage == ESPERANTO) {
            searchView?.queryHint = resources.getString(R.string.search_hint, "esperante")
        } else {
            val langNames = DatabaseHelper(this).getLanguagesHash()
            val currentLangName = langNames[activeLanguage]
            val adverb = currentLangName?.substring(0, currentLangName.length-1) + "e"
            searchView?.queryHint = resources.getString(R.string.search_hint, adverb)
        }
    }

    private fun showAboutDialog(){
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

    private class SearchTask(val context: Context, val adapter: SearchResultAdapter, val language: String)
        : AsyncTask<String, Void, ArrayList<SearchResult>>(){

        override fun doInBackground(vararg params: String?): ArrayList<SearchResult> {
            var results = ArrayList<SearchResult>()
            if(params.isEmpty()) return results
            val searchString = params[0] ?: return results

            val databaseHelper = DatabaseHelper(context)
            try{
                if(language == "eo"){
                    results = databaseHelper.searchWords(searchString)
                } else {
                    results = databaseHelper.searchTranslations(searchString, language)
                }
            } finally {
                databaseHelper.close()
            }

            return results
        }

        override fun onPostExecute(results: ArrayList<SearchResult>?) {
            if(results == null) return
            adapter.receiveDataSet(results)
        }
    }

    private class SearchResultAdapter(val context: Context): BaseAdapter() {
        private var results = ArrayList<SearchResult>()
        private var searchString: String? = null

         fun filter(searchString: String, language: String){
             this.searchString = searchString
            if(searchString == ""){
                results.clear()
            } else {
                SearchTask(context, this, language).execute(searchString)
            }

        }

        fun receiveDataSet(receivedResults: ArrayList<SearchResult>) {
            results = receivedResults
            if(results.count() > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
            val searchString = this.searchString
            val activity = context as SearchActivity
            activity.isSearching = false
            activity.updateBottomPart(searchString != null && !searchString.isEmpty(), results.count())
        }

        override fun getCount() = results.size

        override fun getItem(position: Int): Any = results[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(context)
            val resultRow = if (convertView == null)
                layoutInflater.inflate(R.layout.item_search_entry, parent, false)
            else { convertView }

            val mainWord = resultRow.findViewById<TextView>(R.id.mainWord)
            val definition = resultRow.findViewById<TextView>(R.id.definition)
            val foundEntry = getItem(position)
            var entryId = 0
            var articleId = 0
            if(foundEntry is SearchResult){
                mainWord.text = foundEntry.word
                entryId = foundEntry.id
                articleId = foundEntry.articleId ?: 0
                definition.text = foundEntry.formattedDefinition(null)
            }

            resultRow.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(context, DefinitionActivity::class.java)
                    if(entryId > 0) {
                        intent.putExtra(DefinitionActivity.DEFINITION_ID, entryId)
                        intent.putExtra(DefinitionActivity.ARTICLE_ID, articleId)
                        intent.putExtra(DefinitionActivity.ENTRY_POSITION, position)

                        val bundle = Bundle()
                        bundle.putIntegerArrayList(
                                DefinitionActivity.ENTRIES_LIST,
                                ArrayList(results.map { x -> x.id }))
                        intent.putExtras(bundle)
                        context.startActivity(intent)
                    }

                }
            })

            return resultRow
        }
    }
}

