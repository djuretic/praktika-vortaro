package com.esperantajvortaroj.app.db

import android.content.Context
import android.database.DatabaseUtils
import android.text.TextUtils
import com.esperantajvortaroj.app.SearchResult
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import java.util.ArrayList

class DatabaseHelper : SQLiteAssetHelper {
    companion object {
        val DB_NAME = "vortaro.db"
        val DB_VERSION = 14

        fun getLanguagesHash(context: Context): HashMap<String, String> {
            val helper = DatabaseHelper(context)
            try{
                return helper.getLanguagesHash()
            }  finally {
                helper.close()
            }
        }

        fun databasePath(context: Context): String {
            return context.getApplicationInfo().dataDir + "/databases/${DB_NAME}"
        }
    }

    constructor(context: Context) : super(context, DB_NAME, null, DB_VERSION) {
        setForcedUpgrade()
    }

    fun searchWords(searchString: String, exact: Boolean = false) : ArrayList<SearchResult> {
        var sanitizedString = searchString.replace("%", "")
        if(!exact){
            if(sanitizedString.contains('*')){
               sanitizedString = sanitizedString.replace("*", "%")
            } else {
                sanitizedString = "$sanitizedString%"
            }
        }
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT d.id, d.article_id, w.word, d.definition, d.format
            FROM words w INNER JOIN definitions d ON (w.definition_id = d.id)
            WHERE w.word LIKE ?
            ORDER BY w.id
            LIMIT 50
            """, arrayOf(sanitizedString))
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
        db.close()
        return result
    }

    fun searchTranslations(searchString: String, language: String): ArrayList<SearchResult> {
        var sanitizedString = searchString
        if(searchString.contains('*')){
            sanitizedString = sanitizedString.replace("*", "%")
        } else {
            sanitizedString = "$sanitizedString%"
        }
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT definition_id, word, translation
            FROM translations_$language
            WHERE translation LIKE ?
            ORDER BY id
            LIMIT 50""", arrayOf(sanitizedString))
        val result = arrayListOf<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("word"))
            val word = cursor.getString(cursor.getColumnIndex("translation"))
            val id = cursor.getInt(cursor.getColumnIndex("definition_id"))
            val format = StringFormat.empty()
            result.add(SearchResult(id, null, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return result
    }

    fun definitionById(definitionId: Int): SearchResult?{
        var result: SearchResult? = null
        val db = readableDatabase
        val cursor = readableDatabase.query("definitions", arrayOf("id", "article_id", "words", "definition", "format"),
                "id = ?", arrayOf(""+definitionId), null, null, null)
        if(cursor.count == 1){
            cursor.moveToFirst()
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("words"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val articleId = cursor.getInt(cursor.getColumnIndex("article_id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            result = SearchResult(id, articleId, word, definition, format)
        }
        cursor.close()
        db.close()
        return result
    }

    fun articleById(articleId: Int): ArrayList<SearchResult> {
        //TODO same order as in inside the article
        val db = readableDatabase
        val cursor = db.query("definitions", arrayOf("id", "words", "definition", "format"),
                "article_id = ?", arrayOf(""+articleId), null, null, "position")
        val results = ArrayList<SearchResult>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val definition = cursor.getString(cursor.getColumnIndex("definition"))
            val word = cursor.getString(cursor.getColumnIndex("words"))
            val id = cursor.getInt(cursor.getColumnIndex("id"))
            val format = parseFormat(cursor.getString(cursor.getColumnIndex("format")))
            results.add(SearchResult(id, articleId, word, definition, format))
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return results
    }


    fun translationsByDefinitionId(definitionId: Int, lng: String): List<TranslationResult> {
        val results = mutableListOf<TranslationResult>()
        val cursor = readableDatabase.query("translations_$lng", arrayOf("word", "translation", "snc_index"),
                "definition_id = ?", arrayOf(""+definitionId), null, null, "snc_index")
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val translation = cursor.getString(cursor.getColumnIndex("translation"))
            val sncIndex = cursor.getInt(cursor.getColumnIndex("snc_index"))
            val res = TranslationResult(word, lng, translation, sncIndex)
            results.add(res)
            cursor.moveToNext()
        }
        cursor.close()
        return results
    }

    fun translationsByDefinitionIds(definitionIds: List<Int>, lng: String): List<TranslationResult> {
        val results = mutableListOf<TranslationResult>()
        val cursor = readableDatabase.query(
                "translations_$lng",
                arrayOf("definition_id", "word", "translation", "snc_index"),
                "definition_id IN (" + TextUtils.join(",", definitionIds.map { "?" }) +")"  ,
                definitionIds.map { it.toString() }.toTypedArray(), null, null, "snc_index")
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val word = cursor.getString(cursor.getColumnIndex("word"))
            val translation = cursor.getString(cursor.getColumnIndex("translation"))
            val sncIndex = cursor.getInt(cursor.getColumnIndex("snc_index"))
            val definitionId = cursor.getInt(cursor.getColumnIndex("definition_id"))
            val res = TranslationResult(word, lng, translation, sncIndex, definitionIds.indexOf(definitionId))
            results.add(res)
            cursor.moveToNext()
        }
        cursor.close()
        return results
    }

    fun getLanguages(): ArrayList<Language>{
        val cursor = readableDatabase.query(
                "languages", arrayOf("code", "name", "num_entries"),
                null, null, null, null, null)
        val result = arrayListOf<Language>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val code = cursor.getString(cursor.getColumnIndex("code"))
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val numEntries = cursor.getInt(cursor.getColumnIndex("num_entries"))

            result.add(Language(code, name, numEntries))
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


    private fun parseFormat(string: String): StringFormat {
        val sections = string.split("\n")

        var bold = emptyList<Pair<Int, Int>>()
        var italic = emptyList<Pair<Int, Int>>()
        var ekz = emptyList<Pair<Int, Int>>()
        var fako = emptyList<Pair<Int, Int>>()
        var tld = emptyList<Pair<Int, Int>>()
        for (line in sections){
            if(line.isEmpty()){
                continue
            }
            val parts = line.split(":")
            val list = parts[1].split(";").map {
                val coords = it.split(",")
                Pair(coords[0].toInt(), coords[1].toInt())
            }
            when(parts[0]){
                "italic" -> italic = list
                "bold" -> bold = list
                "ekz" -> ekz = list
                "fako" -> fako = list
                "tld" -> tld = list
            }
        }

        return StringFormat(italic, bold, ekz, fako, tld)
    }

    fun getDiscipline(code: String): String {
        val cursor = readableDatabase.query(
                "disciplines", arrayOf("name"), "code = ?", arrayOf(code.trim()),
                null, null, null)
        var result = code
        if(cursor.count == 1){
            cursor.moveToFirst()
            result = cursor.getString(cursor.getColumnIndex("name"))
        }
        cursor.close()
        return result
    }

    fun getArticleCountDefinitions(articleId: Int): Long {
        return DatabaseUtils.queryNumEntries(
                readableDatabase,
                "definitions", "article_id = ?", arrayOf(""+articleId)
        )
    }

}

data class Language(val code: String, val name: String, val numEntries: Int)

data class StringFormat(
        val italic: List<Pair<Int, Int>>, val bold: List<Pair<Int, Int>>,
        val ekz: List<Pair<Int, Int>>, val fako: List<Pair<Int, Int>>,
        val tld: List<Pair<Int, Int>>) {
    companion object {
        fun empty(): StringFormat {
            return StringFormat(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
}

data class TranslationResult(
        val word: String, val lng: String, val translation: String, val sncIndex: Int,
        // used to create link in the article view's list of translations
        val positionInArticle: Int = 0)
