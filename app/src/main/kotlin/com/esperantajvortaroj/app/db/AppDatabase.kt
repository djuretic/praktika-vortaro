package com.esperantajvortaroj.app.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.esperantajvortaroj.app.SingletonHolder

@Database(entities = arrayOf(SearchHistory::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it, AppDatabase::class.java, "app.db").build()
    })
}

