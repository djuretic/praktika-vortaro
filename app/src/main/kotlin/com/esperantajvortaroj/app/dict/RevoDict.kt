package com.esperantajvortaroj.app.dict

import android.content.Context
import com.esperantajvortaroj.app.*
import com.esperantajvortaroj.app.db.DatabaseHelper

class RevoDict : DictInterface<Object> {
    override fun search(context: Context, searchString: String, language: String, viewModel: SearchableViewModel<Object>?): SearchResultStatus {
        val result = SearchResultStatus(ArrayList(), language, null)
        val databaseHelper = DatabaseHelper(context)
        try{
            result.results.addAll(doSearch(databaseHelper, searchString, language))
            // try with other languages
            if(result.results.isEmpty()){
                val langPrefs = PreferenceHelper.getStringSet(context, SettingsActivity.KEY_LANGUAGES_PREFERENCE)
                val mutableLangPrefs = LinkedHashSet<String>(langPrefs)
                if(language != "eo")
                    mutableLangPrefs.add("eo")
                else
                    mutableLangPrefs.remove(language)
                for(lang in mutableLangPrefs){
                    result.results.addAll(doSearch(databaseHelper, searchString, lang))
                    if(result.results.isNotEmpty()) {
                        result.usedLang = lang
                        break
                    }
                }
            }
        } finally {
            databaseHelper.close()
        }
        return result
    }

    private fun doSearch(databaseHelper: DatabaseHelper, searchString: String, lang: String): ArrayList<SearchResult>{
        if(lang == "eo"){
            return databaseHelper.searchWords(searchString)
        } else {
            return databaseHelper.searchTranslations(searchString, lang)
        }
    }
}