package com.esperantajvortaroj.app

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import com.esperantajvortaroj.app.db.SearchHistory
import com.esperantajvortaroj.app.db.SearchHistoryDao

class SearchHistoryRepository(private val searchHistoryDao : SearchHistoryDao) {
    val allHistory: LiveData<List<SearchHistory>> = searchHistoryDao.getAll()

    @WorkerThread
    fun insertOne(searchHistory: SearchHistory) {
        searchHistoryDao.insertOne(searchHistory)
    }

    @WorkerThread
    fun deleteOlderEntries() {
        searchHistoryDao.deleteOldestEntries()
    }

    @WorkerThread
    fun updateLast(word: String) {
        searchHistoryDao.updateLast(word)
    }

    @WorkerThread
    fun deleteOne(entry: SearchHistory) {
        searchHistoryDao.deleteOne(entry)
    }
}