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
        val cursor = db.rawQuery("""
            SELECT id, word, definition, format
            FROM words
            WHERE word LIKE ?
            ORDER BY word COLLATE NOCASE
            LIMIT 50""", arrayOf(searchString+"%"))
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

    fun searchTranslations(searchString: String, language: String): ArrayList<SearchResult> {
        val db = writableDatabase
        val cursor = db.rawQuery("""
            SELECT word_id, word, translation
            FROM translations
            WHERE lng = ?
            AND translation LIKE ?
            ORDER BY word COLLATE NOCASE
            LIMIT 50""", arrayOf(language, searchString+"%"))
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("word"))
            val word = cursor.getString(cursor.getColumnIndex("translation"))
            val id = cursor.getInt(cursor.getColumnIndex("word_id"))
            val format = StringFormat(emptyList(), emptyList())
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

    fun translationsByWordId(wordId: Int, langPrefs: MutableSet<String>): List<TranslationResult> {
        val results = mutableListOf<TranslationResult>()
        val db = writableDatabase
        if(langPrefs.isEmpty()){
            return results
        }
        var sql = "SELECT word, lng, translation FROM translations WHERE word_id = ? and lng IN ("
        sql += langPrefs.joinToString(transform= {a -> "?"})
        sql += ")"

        val cursor = db.rawQuery(sql, arrayOf(""+wordId) + langPrefs)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val lng = cursor.getString(cursor.getColumnIndex("lng"))
            val translation = cursor.getString(cursor.getColumnIndex("translation"))
            val res = TranslationResult(word, lng, translation)
            results.add(res)
            cursor.moveToNext()
        }
        cursor.close()
        return results
    }


    private fun parseFormat(string: String): StringFormat{
        val sections = string.split("\n")

        var bold = emptyList<Pair<Int, Int>>()
        var italic = emptyList<Pair<Int, Int>>()
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
                italic = list
            } else if (parts[0] == "bold"){
                bold = list
            }
        }

        return StringFormat(italic, bold)
    }

}

data class StringFormat(val italic: List<Pair<Int, Int>>, val bold: List<Pair<Int, Int>>)
data class SearchResult(val id: Int, val word: String, val definition: String, val format: StringFormat?)
data class TranslationResult(val word: String, val lng: String, val translation: String)