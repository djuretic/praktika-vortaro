package com.esperantajvortaroj.app.dict

import android.content.Context
import com.esperantajvortaroj.app.SearchResultStatus
import com.esperantajvortaroj.app.SearchableViewModel

interface DictInterface<T> {
    fun search(context: Context, searchString: String, language: String, viewModel: SearchableViewModel<T>?): SearchResultStatus
}