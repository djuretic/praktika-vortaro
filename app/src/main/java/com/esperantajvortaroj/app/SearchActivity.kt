package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.esperantajvortaroj.app.db.DatabaseHelper
import com.esperantajvortaroj.app.db.SearchHistory

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync


class SearchActivity : AppCompatActivity() {
    private var searchAdapter : SearchResultAdapter? = null
    private var searchHistoryAdapter : SearchHistoryAdapter? = null
    private val ESPERANTO = "eo"
    private var activeLanguage = ESPERANTO
    private var searchView: SearchView? = null
    private var isSearching = false
    /* Used to avoid generating (repeated) search history when visiting the history */
    private var isFromSearchHistory = false
    /* Used when coming back from DefinitionActivity*/
    private var resetSearch = false
    private var lastSearchQuery: String? = null
    private lateinit var searchHistoryViewModel: SearchHistoryViewModel

    companion object {
        const val RESET_SEARCH = "reset_search"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(appToolbar)

        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        activeLanguage = PreferenceHelper.getString(this, SettingsActivity.ACTIVE_LANGUAGE, ESPERANTO)

        searchAdapter = SearchResultAdapter(this)
        searchResults.adapter = searchAdapter

        searchHistoryAdapter = SearchHistoryAdapter(this)
        searchHistoryList.adapter = searchHistoryAdapter
        registerForContextMenu(searchHistoryList)
        searchHistoryList.setOnItemClickListener { parent, view, position, id ->
            if (view is TextView) {
                isFromSearchHistory = true
                searchView?.setQuery(view.text.toString(), true)
                searchView?.isFocusableInTouchMode = true
                searchView?.requestFocus()
                isFromSearchHistory = false
            }
        }

        searchHistoryViewModel = ViewModelProviders.of(this).get(SearchHistoryViewModel::class.java)
        searchHistoryViewModel.allHistory.observe(this, Observer { history ->
            history?.let { searchHistoryAdapter?.receiveDataSet(history) }
        })

        versionChecks()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.history_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val targetView = info.targetView
        if(targetView !is SearchHistoryTextView){
            return super.onContextItemSelected(item)
        }

        when(item?.itemId) {
            R.id.copyHistoryEntry -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText("difino", targetView.text.toString())
                return true
            }
            R.id.deleteHistoryEntry -> {
                val entry = targetView.historyEntry
                if(entry != null) {
                    doAsync {
                        searchHistoryViewModel.deleteOne(entry)
                    }
                }
            }
            else -> return super.onContextItemSelected(item)
        }
        return super.onContextItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        resetSearch = intent?.getBooleanExtra(RESET_SEARCH, false) ?: false
    }

    override fun onPostResume() {
        super.onPostResume()
        if(resetSearch){
            searchView?.setQuery("", true)
            searchView?.isFocusableInTouchMode = true
            searchView?.requestFocus()
            resetSearch = false
        }
    }

    private fun versionChecks() {
        val sharedPref = PreferenceHelper.defaultSharedPreferences(this)
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
                    val lastQuery = lastSearchQuery
                    var lastQueryIsPrefix = query != null && lastQuery != null && query.startsWith(lastQuery)
                    if(query == null || query.isEmpty()) {
                        isSearching = false
                        updateBottomPart(false, 0)
                        return true
                    }

                    var text = query.trim()
                    if(activeLanguage == ESPERANTO){
                        text = Utils.addHats(text)
                    }
                    searchAdapter?.filter(text, activeLanguage, saveHistory = !isFromSearchHistory, isUpdate = lastQueryIsPrefix)
                    isSearching = true
                    updateBottomPart(true, searchAdapter?.count ?: 0)
                    lastSearchQuery = query
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

    private fun updateBottomPart(enteredText: Boolean, resultsCount: Int, originalLang: String? = null, usedLang: String? = null) {
        searchResults.setSelection(0)
        if(isSearching){
            progressBarSearch.visibility = View.VISIBLE
            noResultsFound.visibility = View.GONE
            searchResults.visibility = View.GONE
            searchHistoryList.visibility = View.GONE
            return
        }
        progressBarSearch.visibility = View.GONE
        if(!enteredText){
            noResultsFound.visibility = View.GONE
            searchResults.visibility = View.GONE
            searchHistoryList.visibility = View.VISIBLE
        } else if(resultsCount == 0) {
            noResultsFound.text = resources.getString(R.string.no_results_found)
            noResultsFound.visibility = View.VISIBLE
            searchResults.visibility = View.GONE
            searchHistoryList.visibility = View.GONE
        } else {
            if(usedLang == null){
                noResultsFound.text = resources.getString(R.string.no_results_found)
                noResultsFound.visibility = View.GONE
            } else {
                val langHash = DatabaseHelper.getLanguagesHash(this)
                noResultsFound.text = resources.getString(
                        R.string.results_found_in_another_language,
                        Utils.languageName(langHash, originalLang),
                        Utils.languageName(langHash, usedLang))
                noResultsFound.visibility = View.VISIBLE
            }

            searchResults.visibility = View.VISIBLE
            searchHistoryList.visibility = View.GONE
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
                val langPrefs = PreferenceHelper.getStringSet(this, SettingsActivity.KEY_LANGUAGES_PREFERENCE)

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

                PreferenceHelper.putString(this, SettingsActivity.ACTIVE_LANGUAGE, activeLanguage)

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
            R.id.font_size_setting -> {
                showFontSizeDialog()
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

    private fun showFontSizeDialog() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fontSize = sharedPrefs.getInt(SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE)

        val picker = NumberPicker(this)
        picker.minValue = 5
        picker.maxValue = 50
        picker.value = fontSize

        val layout = FrameLayout(this)
        layout.addView(picker, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ))

        val builder = AlertDialog.Builder(this)
        builder.setView(layout)
        builder.setTitle("Elektu tiparan grandon")
        builder.setPositiveButton(R.string.close_dialog) { dialog, which ->
            val newFontSize = picker.value
            val editor = sharedPrefs.edit()
            editor.putInt(SettingsActivity.FONT_SIZE, newFontSize)
            editor.apply()
            dialog?.dismiss()
            searchResults.invalidateViews()
        }
        builder.show()
    }

    private fun startLanguageActivity() {
        val intent = Intent(this, SelectTranslationLanguageActivity::class.java)
        startActivity(intent)
    }

    private fun updateSearchQueryHint() {
        if (activeLanguage == ESPERANTO) {
            searchView?.queryHint = resources.getString(R.string.search_hint, "esperante")
        } else {
            val langNames = DatabaseHelper.getLanguagesHash(this)
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
        : AsyncTask<String, Void, SearchResultStatus>(){

        override fun doInBackground(vararg params: String?): SearchResultStatus {
            val result = SearchResultStatus(ArrayList(), language, null)
            if(params.isEmpty()) return result
            val searchString = params[0] ?: return result

            val databaseHelper = DatabaseHelper(context)
            try{
                result.results = doSearch(databaseHelper, searchString, language)
                // try with other languages
                if(result.results.isEmpty()){
                    val langPrefs = PreferenceHelper.getStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE)
                    val mutableLangPrefs = LinkedHashSet<String>(langPrefs)
                    if(language != "eo")
                        mutableLangPrefs.add("eo")
                    else
                        mutableLangPrefs.remove(language)
                    for(lang in mutableLangPrefs){
                        result.results = doSearch(databaseHelper, searchString, lang)
                        if(result.results.isNotEmpty()) {
                            result.usedLang = lang
                            return result
                        }
                    }
                }
            } finally {
                databaseHelper.close()
            }

            return result
        }

        fun doSearch(databaseHelper: DatabaseHelper, searchString: String, lang: String): ArrayList<SearchResult>{
            if(lang == "eo"){
                return databaseHelper.searchWords(searchString)
            } else {
                return databaseHelper.searchTranslations(searchString, lang)
            }
        }

        override fun onPostExecute(results: SearchResultStatus?) {
            if(results == null) return
            adapter.receiveDataSet(results)
        }
    }

    private class SearchResultAdapter(val context: Context): BaseAdapter() {
        private var results = ArrayList<SearchResult>()
        private var searchString: String? = null

         fun filter(searchString: String, language: String, saveHistory: Boolean, isUpdate: Boolean){
             this.searchString = searchString
            if(searchString == ""){
                results.clear()
            } else {
                val activity = context as SearchActivity
                if(saveHistory) {
                    activity.updateHistory(searchString, isUpdate)
                }

                SearchTask(context, this, language).execute(searchString)
            }

        }

        fun receiveDataSet(receivedResults: SearchResultStatus) {
            results = receivedResults.results
            if(results.count() > 0)
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
            val searchString = this.searchString
            val activity = context as SearchActivity
            activity.isSearching = false
            activity.updateBottomPart(
                    searchString != null && !searchString.isEmpty(),
                    results.count(), receivedResults.originalLang, receivedResults.usedLang)
        }

        override fun getCount() = results.size

        override fun getItem(position: Int): Any = results[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(context)
            val resultRow = if (convertView == null)
                layoutInflater.inflate(R.layout.item_search_entry, parent, false)
            else { convertView }

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val fontSize = sharedPrefs.getInt(SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE)

            val mainWord = resultRow.findViewById<TextView>(R.id.mainWord)
            mainWord.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
            val definition = resultRow.findViewById<TextView>(R.id.definition)
            definition.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
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

    private fun updateHistory(searchString: String, update: Boolean) {
        doAsync {
            if (update) {
                searchHistoryViewModel.updateLast(searchString)
            } else {
                searchHistoryViewModel.insert(SearchHistory(0, searchString))
                searchHistoryViewModel.deleteOlderEntries()
            }
        }
    }
}

data class SearchResultStatus(var results: ArrayList<SearchResult>, val originalLang: String, var usedLang: String?)