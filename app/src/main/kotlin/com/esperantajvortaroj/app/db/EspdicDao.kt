package com.esperantajvortaroj.app.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface EspdicDao {
    @Query("SELECT * FROM Espdic WHERE eo LIKE :word ORDER BY id DESC LIMIT 50")
    fun getEo(word: String): List<Espdic>

    @Query("SELECT * FROM Espdic WHERE en LIKE :word ORDER BY id DESC LIMIT 50")
    fun getEn(word: String): List<Espdic>
}