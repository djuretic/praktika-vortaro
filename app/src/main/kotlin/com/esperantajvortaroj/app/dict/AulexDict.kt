package com.esperantajvortaroj.app.dict

import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import com.esperantajvortaroj.app.*

class AulexDict : DictInterface {
    override fun search(context: Context, searchString: String, language: String, viewModel: AndroidViewModel?): SearchResultStatus {
        if (viewModel !is AulexViewModel) {
            throw Exception("Dictionary cast error")
        }
        val result = SearchResultStatus(ArrayList(), language, null)
        if (viewModel == null) {
            return result
        }
        val espdicResults = viewModel.search(Utils.sanitizeLikeQuery(searchString, exact = false), language)
        if (language == "eo") {
            result.results = ArrayList(espdicResults.map { it -> SearchResult(Dictionary.AULEX, it.id, 0, it.eo, it.es, null) })
        } else {
            result.results = ArrayList(espdicResults.map { it -> SearchResult(Dictionary.AULEX, it.id, 0, it.es, it.eo, null) })
        }
        return result
    }
}