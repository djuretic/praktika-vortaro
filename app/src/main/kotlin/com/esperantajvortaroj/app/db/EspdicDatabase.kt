package com.esperantajvortaroj.app.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.Environment
import com.esperantajvortaroj.app.SingletonHolder

@Database(entities = arrayOf(Espdic::class), version = 1)
abstract class EspdicDatabase : RoomDatabase() {
    abstract fun espdicDao(): EspdicDao

    companion object : SingletonHolder<EspdicDatabase, Context>({
        val sdDir = Environment.getExternalStorageDirectory()
        val path = "$sdDir/Download/espdic.db"
        Room.databaseBuilder(it, EspdicDatabase::class.java, path).build()
    })
}
