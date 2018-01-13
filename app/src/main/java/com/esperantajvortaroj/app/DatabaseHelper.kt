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
        val cursor = db.rawQuery("SELECT id, word, definition, format FROM words WHERE word LIKE ? ORDER BY word COLLATE NOCASE LIMIT 50", arrayOf(searchString+"%"))
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            result.add(SearchResult(id, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    fun wordById(wordId: Int): SearchResult?{
        val db = writableDatabase
        var result: SearchResult? = null
        val cursor = db.rawQuery("SELECT id, word, definition, format FROM words WHERE id = ?", arrayOf(""+wordId))
        if(cursor.count == 1){
            cursor.moveToFirst()
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            result = SearchResult(id, word, definition, format)
        }
        cursor.close()
        return result
    }

    private fun parseFormat(string: String): StringFormat{
        val sections = string.split("\n")

        val format = StringFormat(emptyList(), emptyList())
        for (line in sections){
            if(line.isEmpty()){
                continue
            }
            val parts = line.split(":")
            val list = parts[1].split(";").map {
                val coords = it.split(",")
                Pair(coords[0].toInt(), coords[1].toInt())
            }
            if(parts[0] == "italic"){
                format.italic = list
            } else if (parts[0] == "bold"){
                format.bold = list
            }
        }
        return format
    }
}

data class StringFormat(var italic: List<Pair<Int, Int>>, var bold: List<Pair<Int, Int>>)
data class SearchResult(val id: Int, val word: String, val definition: String, val format: StringFormat?)