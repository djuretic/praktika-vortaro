package com.esperantajvortaroj.app

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

class SearchHistoryViewModel(application: Application) : AndroidViewModel(application) {
    val searchHistoryDao: SearchHistoryDao = AppDatabase.getInstance(application).searchHistoryDao()
    val allHistory: LiveData<List<SearchHistory>>

    init {
        allHistory = searchHistoryDao.getAll()
    }

    fun insert(searchHistory: SearchHistory) {
        searchHistoryDao.insertOne(searchHistory)
    }
}