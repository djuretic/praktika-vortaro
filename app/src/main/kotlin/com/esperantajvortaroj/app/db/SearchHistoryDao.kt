package com.esperantajvortaroj.app.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory ORDER BY id DESC")
    fun getAll(): LiveData<List<SearchHistory>>

    @Insert
    fun insertOne(searchHistory: SearchHistory)

    @Query("UPDATE SearchHistory SET word = :word WHERE id=(SELECT MAX(id) FROM SearchHistory)")
    fun updateLast(word: String)

    @Delete
    fun deleteOne(searchHistory: SearchHistory)

    @Query("DELETE FROM SearchHistory WHERE id <= (SELECT MAX(id) - :numEntries FROM SearchHistory)")
    fun deleteOldestEntries(numEntries: Int = 50)
}
