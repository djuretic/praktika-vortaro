package com.esperantajvortaroj.app.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface AulexDao {
    @Query("SELECT * FROM Aulex WHERE eo LIKE :word ORDER BY id DESC LIMIT 50")
    fun getEo(word: String): List<Aulex>

    @Query("SELECT * FROM Aulex WHERE es LIKE :word ORDER BY id DESC LIMIT 50")
    fun getEs(word: String): List<Aulex>

    @Query("SELECT * FROM Aulex WHERE id = :id")
    fun getById(id: Int): Aulex
}