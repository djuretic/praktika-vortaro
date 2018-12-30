package com.esperantajvortaroj.app

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory")
    fun getAll(): LiveData<List<SearchHistory>>

    @Insert
    fun insertOne(searchHistory: SearchHistory)

    @Delete
    fun deleteOne(searchHistory: SearchHistory)
}
