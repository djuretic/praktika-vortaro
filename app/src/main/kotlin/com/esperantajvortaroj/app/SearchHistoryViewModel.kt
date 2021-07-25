package com.esperantajvortaroj.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.esperantajvortaroj.app.db.AppDatabase
import com.esperantajvortaroj.app.db.SearchHistory

class SearchHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository : SearchHistoryRepository
    val allHistory: LiveData<List<SearchHistory>>

    init {
        val searchHistoryDao = AppDatabase.getInstance(application).searchHistoryDao()
        repository = SearchHistoryRepository(searchHistoryDao)
        allHistory = repository.allHistory
    }

    fun insert(searchHistory: SearchHistory) {
        repository.insertOne(searchHistory)
    }

    fun deleteOlderEntries() {
        repository.deleteOlderEntries()
    }

    fun updateLast(word: String) {
        repository.updateLast(word)
    }

    fun deleteOne(entry: SearchHistory) {
        repository.deleteOne(entry)
    }
}