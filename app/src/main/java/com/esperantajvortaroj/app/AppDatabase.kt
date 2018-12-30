package com.esperantajvortaroj.app

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(SearchHistory::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object : SingletonHolder<AppDatabase, Context>({
        Room.databaseBuilder(it, AppDatabase::class.java, "app.db").build()
    })
}

