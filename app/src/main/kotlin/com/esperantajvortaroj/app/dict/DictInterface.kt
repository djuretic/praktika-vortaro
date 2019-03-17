package com.esperantajvortaroj.app.dict

import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import com.esperantajvortaroj.app.SearchResultStatus

interface DictInterface {
    fun search(context: Context, searchString: String, language: String, viewModel: AndroidViewModel?): SearchResultStatus
}