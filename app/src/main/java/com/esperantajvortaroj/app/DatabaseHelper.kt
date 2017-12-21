package com.esperantajvortaroj.app

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import java.util.ArrayList

class DatabaseHelper : SQLiteAssetHelper {
    companion object {
        val DB_NAME = "vortaro.db"
        val DB_VERSION = 1
    }

    constructor(context: Context) : super(context, DB_NAME, null, DB_VERSION)

    fun searchWords(searchString: String) : ArrayList<SearchResult> {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM words WHERE word LIKE ? ORDER BY word COLLATE NOCASE LIMIT 50", arrayOf(searchString+"%"))
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            result.add(SearchResult(id, word, definition))
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    fun wordById(wordId: Int): SearchResult?{
        val db = writableDatabase
        var result: SearchResult? = null
        val cursor = db.rawQuery("SELECT * FROM words WHERE id = ?", arrayOf(""+wordId))
        if(cursor.count == 1){
            cursor.moveToFirst()
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            result = SearchResult(id, word, definition)
        }
        cursor.close()
        return result
    }
}

data class SearchResult(val id: Int, val word: String, val definition: String)