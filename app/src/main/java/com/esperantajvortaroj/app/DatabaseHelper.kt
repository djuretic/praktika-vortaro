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
        val cursor = readableDatabase.query(
                "words", arrayOf("id", "article_id", "word", "definition", "format"),
                "word LIKE ?", arrayOf(searchString+"%"), null, null, "id", "50")
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val articleId = cursor.getInt(cursor.getColumnIndex("article_id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            result.add(SearchResult(id, articleId, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    fun searchTranslations(searchString: String, language: String): ArrayList<SearchResult> {
        val db = writableDatabase
        val cursor = db.rawQuery("""
            SELECT word_id, word, translation
            FROM translations_$language
            WHERE translation LIKE ?
            ORDER BY id
            LIMIT 50""", arrayOf(searchString+"%"))
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("word"))
            val word = cursor.getString(cursor.getColumnIndex("translation"))
            val id = cursor.getInt(cursor.getColumnIndex("word_id"))
            val format = StringFormat(emptyList(), emptyList())
            result.add(SearchResult(id, null, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    fun wordById(wordId: Int): SearchResult?{
        var result: SearchResult? = null
        val cursor = readableDatabase.query("words", arrayOf("id", "article_id", "word", "definition", "format"),
                "id = ?", arrayOf(""+wordId), null, null, null)
        if(cursor.count == 1){
            cursor.moveToFirst()
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val articleId = cursor.getInt(cursor.getColumnIndex("article_id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            result = SearchResult(id, articleId, word, definition, format)
        }
        cursor.close()
        return result
    }

    fun articleById(articleId: Int): ArrayList<SearchResult> {
        //TODO same order as in inside the article
        val cursor = readableDatabase.query("words", arrayOf("id", "article_id", "word", "definition", "format"),
                "article_id = ?", arrayOf(""+articleId), null, null, "id")
        val results = ArrayList<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val articleId = cursor.getInt(cursor.getColumnIndex("article_id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            results.add(SearchResult(id, articleId, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        return results
    }


    fun translationsByWordId(wordId: Int, lng: String): List<TranslationResult> {
        val results = mutableListOf<TranslationResult>()
        val db = writableDatabase
        var sql = "SELECT word, translation FROM translations_$lng WHERE word_id = ?"
        //sql += langPrefs.joinToString(transform= {a -> "?"})

        val cursor = db.rawQuery(sql, arrayOf(""+wordId))
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val translation = cursor.getString(cursor.getColumnIndex("translation"))
            val res = TranslationResult(word, lng, translation)
            results.add(res)
            cursor.moveToNext()
        }
        cursor.close()
        return results
    }

    fun getLanguages(): ArrayList<Language>{
        val cursor = readableDatabase.query(
                "languages", arrayOf("code", "name"),
                null, null, null, null, null)
        val result = arrayListOf<Language>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val code = cursor.getString(cursor.getColumnIndex("code"))
            val name = cursor.getString(cursor.getColumnIndex("name"))

            result.add(Language(code, name))
            cursor.moveToNext()
        }
        cursor.close()
        return result
    }

    fun getLanguagesHash(): HashMap<String, String>{
        val cursor = readableDatabase.query(
                "languages", arrayOf("code", "name"),
                null, null, null, null, null)
        // order matters here, so we use LinkedHashMap
        val result = LinkedHashMap<String, String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val code = cursor.getString(cursor.getColumnIndex("code"))
            val name = cursor.getString(cursor.getColumnIndex("name"))

            result.put(code, name)
            cursor.moveToNext()
        }
        cursor.close()
        return result
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

data class Language(val code: String, val name: String)
data class StringFormat(val italic: List<Pair<Int, Int>>, val bold: List<Pair<Int, Int>>)
data class SearchResult(
        val id: Int, val articleId: Int?, val word: String, val definition: String, val format: StringFormat?)
data class TranslationResult(val word: String, val lng: String, val translation: String)