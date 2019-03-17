package com.esperantajvortaroj.app.dict

import android.content.Context
import com.esperantajvortaroj.app.*
import com.esperantajvortaroj.app.db.Espdic

class EspdicDict : DictInterface<Espdic> {
    override fun search(context: Context, searchString: String, language: String, viewModel: SearchableViewModel<Espdic>?): SearchResultStatus {
        val result = SearchResultStatus(ArrayList(), language, null)
        if (viewModel == null) {
            return result
        }
        val espdicResults = viewModel.search(Utils.sanitizeLikeQuery(searchString, exact = false), language)
        if (language == "eo") {
            result.results = ArrayList(espdicResults.map { it -> SearchResult(Dictionary.ESPDIC, it.id, 0, it.eo, it.en, null) })
        } else {
            result.results = ArrayList(espdicResults.map { it -> SearchResult(Dictionary.ESPDIC, it.id, 0, it.en, it.eo, null) })
        }
        return result
    }
}