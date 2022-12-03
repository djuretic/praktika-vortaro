package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.content.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.esperantajvortaroj.app.db.DatabaseHelper
import com.esperantajvortaroj.app.db.SearchHistory

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


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

        AppCompatDelegate.setDefaultNightMode(PreferenceHelper.getNightMode(this))

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        activeLanguage = PreferenceHelper.getActiveLanguage(this, ESPERANTO)

        searchAdapter = SearchResultAdapter(this)
        searchResults.adapter = searchAdapter

        searchHistoryAdapter = SearchHistoryAdapter(this)
        searchHistoryAdapter?.setOnDelete { view ->
            if(view is SearchHistoryView) {
                deleteHistoryEntry(view)
            }
        }
        searchHistoryList.adapter = searchHistoryAdapter
        registerForContextMenu(searchHistoryList)
        searchHistoryList.setOnItemClickListener { _, view, _, _ ->
            if (view is SearchHistoryView) {
                isFromSearchHistory = true
                searchView?.setQuery(view.word.toString(), true)
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

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val targetView = info.targetView
        if(targetView !is SearchHistoryView){
            return super.onContextItemSelected(item)
        }

        when(item.itemId) {
            R.id.copyHistoryEntry -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("difino", targetView.word.toString()))
                return true
            }
            R.id.deleteHistoryEntry -> {
                deleteHistoryEntry(targetView)
            }
            else -> return super.onContextItemSelected(item)
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteHistoryEntry(targetView: SearchHistoryView) {
        val entry = targetView.historyEntry
        if (entry != null) {
            doAsync {
                searchHistoryViewModel.deleteOne(entry)
            }
        }
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
        val versionCode = PreferenceHelper.getVersionCode(this)
        if (versionCode == 0){
            // first run
            startLanguageActivity()
        }
        if (versionCode != BuildConfig.VERSION_CODE) {
            PreferenceHelper.setVersionCode(this, BuildConfig.VERSION_CODE)
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
                    val lastQueryIsPrefix = query != null && lastQuery != null && query.startsWith(lastQuery)
                    val lastQueryIsMoreComplete = query != null && lastQuery != null && lastQuery.startsWith(query)
                    if(query == null || query.isEmpty()) {
                        isSearching = false
                        updateBottomPart(false, 0)
                        return true
                    }

                    var text = query.trim()
                    if(activeLanguage == ESPERANTO){
                        text = Utils.addHats(text)
                    }
                    searchAdapter?.filter(text, activeLanguage,
                            saveHistory = !isFromSearchHistory && !lastQueryIsMoreComplete,
                            isUpdate = lastQueryIsPrefix)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
                return true
            }
            R.id.change_search_language -> {
                val langPrefs = PreferenceHelper.getLanguagesPreference(this)

                activeLanguage = if(activeLanguage == ESPERANTO && langPrefs.isNotEmpty()){
                    langPrefs.elementAt(0)
                } else if (activeLanguage != ESPERANTO){
                    val currentIndex = langPrefs.indexOf(activeLanguage)
                    if(currentIndex < 0 || currentIndex >= langPrefs.size - 1){
                        ESPERANTO
                    } else {
                        langPrefs.elementAt(currentIndex+1)
                    }
                } else {
                    ESPERANTO
                }
                updateSearchQueryHint()

                PreferenceHelper.setActiveLanguage(this, activeLanguage)

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
            R.id.night_mode_setting -> {
                showNightModeDialog()
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
        val fontSize = PreferenceHelper.getFontSize(this)

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
        builder.setPositiveButton(R.string.close_dialog) { dialog, _ ->
            val newFontSize = picker.value
            PreferenceHelper.setFontSize(this, newFontSize)
            dialog?.dismiss()
            searchResults.invalidateViews()
        }
        builder.show()
    }

    private fun showNightModeDialog() {
        var nightMode = PreferenceHelper.getNightMode(this)
        val layout = FrameLayout(this)

        val items = arrayOf("Aŭtomata", "Malhela", "Hela")
        val modes = arrayOf(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO
        )

        val builder = AlertDialog.Builder(this, R.style.CustomAlert)
        builder.setView(layout)
        builder.setTitle(R.string.dark_mode)
        builder.setSingleChoiceItems(items, modes.indexOf(nightMode)) { _: DialogInterface, i: Int ->
            nightMode = modes[i]
        }
        builder.setPositiveButton("Fermi") { dialog, _ ->
            PreferenceHelper.setNightMode(this, nightMode)
            AppCompatDelegate.setDefaultNightMode(nightMode)
            delegate.applyDayNight()
            dialog.dismiss()
            recreate()
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
        val revoVersion = DatabaseHelper.getRevoVersion(this)
        val builder = AlertDialog.Builder(this)
        val title = resources.getString(R.string.app_name)
        val message = Utils.fromHtml("""
            <p>© 2018-2022 Dušan Juretić</p>
            <p>Datumbazo: <a href="http://www.reta-vortaro.de/revo">Reta Vortaro</a><br/>Versio: ${revoVersion}</p>
            <p>Inspirita de <a href="https://play.google.com/store/apps/details?id=uk.co.busydoingnothing.prevo">PReVo</a></p>
        """)
        val spannableString = SpannableString(message)
        Linkify.addLinks(spannableString, Linkify.ALL)

        val dialog = builder.setMessage(message).setTitle("Pri $title")
                .setPositiveButton(R.string.close_dialog, null)
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message).movementMethod = LinkMovementMethod.getInstance()
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

                doAsync {
                    val result = SearchResultStatus(ArrayList(), language, null)

                    val databaseHelper = DatabaseHelper(context)
                    databaseHelper.use { databaseHelper ->
                        result.results = doSearch(databaseHelper, searchString, language)
                        // try with other languages
                        if(result.results.isEmpty()){
                            val langPrefs = PreferenceHelper.getLanguagesPreference(context)
                            val mutableLangPrefs = LinkedHashSet<String>(langPrefs)
                            if(language != "eo")
                                mutableLangPrefs.add("eo")
                            else
                                mutableLangPrefs.remove(language)
                            for(lang in mutableLangPrefs){
                                result.results = doSearch(databaseHelper, searchString, lang)
                                if(result.results.isNotEmpty()) {
                                    result.usedLang = lang
                                    break
                                }
                            }
                        }
                    }
                    if (result != null) {
                        uiThread {
                            receiveDataSet(result)
                        }

                    }
                }
            }
        }

        private fun doSearch(databaseHelper: DatabaseHelper, searchString: String, lang: String): ArrayList<SearchResult>{
            return if(lang == "eo"){
                databaseHelper.searchWords(searchString)
            } else {
                databaseHelper.searchTranslations(searchString, lang)
            }
        }

        fun receiveDataSet(receivedResults: SearchResultStatus) {
            results = receivedResults.results
            if(results.isNotEmpty())
                notifyDataSetChanged()
            else
                notifyDataSetInvalidated()
            val searchString = this.searchString
            val activity = context as SearchActivity
            activity.isSearching = false
            activity.updateBottomPart(
                    searchString != null && searchString.isNotEmpty(),
                    results.count(), receivedResults.originalLang, receivedResults.usedLang)
        }

        override fun getCount() = results.size

        override fun getItem(position: Int): Any = results[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(context)
            val resultRow =
                convertView ?: layoutInflater.inflate(R.layout.item_search_entry, parent, false)
            
            val fontSize = PreferenceHelper.getFontSize(context)

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

            resultRow.setOnClickListener {
                val intent = Intent(context, DefinitionActivity::class.java)
                if (entryId > 0) {
                    intent.putExtra(DefinitionActivity.DEFINITION_ID, entryId)
                    intent.putExtra(DefinitionActivity.ARTICLE_ID, articleId)
                    intent.putExtra(DefinitionActivity.ENTRY_POSITION, position)

                    val bundle = Bundle()
                    bundle.putIntegerArrayList(
                        DefinitionActivity.ENTRIES_LIST,
                        ArrayList(results.map { x -> x.id })
                    )
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                }
            }

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