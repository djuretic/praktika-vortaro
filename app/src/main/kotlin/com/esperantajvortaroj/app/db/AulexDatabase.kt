package com.esperantajvortaroj.app.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.Environment
import com.esperantajvortaroj.app.SingletonHolder

@Database(entities = arrayOf(Aulex::class), version = 1)
abstract class AulexDatabase : RoomDatabase() {
    abstract fun aulexDao(): AulexDao

    companion object : SingletonHolder<AulexDatabase, Context>({
        val sdDir = Environment.getExternalStorageDirectory()
        val path = "$sdDir/Download/aulex.db"
        Room.databaseBuilder(it, AulexDatabase::class.java, path).build()
    })
}
