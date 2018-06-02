package com.esperantajvortaroj.app

import android.text.SpannableString
import android.text.TextUtils
import android.text.style.UnderlineSpan

val mapping = hashMapOf(
    'c' to 'ĉ',
    'g' to 'ĝ',
    'h' to 'ĥ',
    'j' to 'ĵ',
    's' to 'ŝ',
    'u' to 'ŭ')

object Utils {
    fun addHats(text: String): String{
        if(text.length <= 1){
            return text
        }
        val baseText = text.toLowerCase()
        var pos = 0
        var res = ""
        while (pos < baseText.length - 1){
            val char = baseText[pos]
            if(mapping.containsKey(char) && baseText[pos+1] == 'x'){
                res += mapping.get(char)
                pos += 2
            } else {
                res += char
                pos += 1
            }
        }
        if(pos == baseText.length - 1){
            res += baseText[pos]
        }
        return res
    }

    fun getWholeWord(text: CharSequence, position: Int): String? {
        val boundaries = " \n:;;.,•?!()[]{}'\"„“"
        if (position >= text.length || text[position] in boundaries) return null
        var firstPos = position
        var lastPos = position

        for(i in position downTo 0 step 1){
            if(text[i] in boundaries) {
                break
            }
            firstPos = i
        }
        for(i in position until text.length){
            if(text[i] in boundaries) {
                break
            }
            lastPos = i
        }
        return text.substring(firstPos..lastPos)
    }

    fun getPossibleBaseWords(word: CharSequence): List<String> {
        val words = arrayListOf(word.toString())
        if(word.length <= 3){
            if(word == "ajn") return words
            if(word.length == 3 && word.endsWith("n")){
                words.add(word.substring(0 until word.length - 1))
            }
            return words
        }

        if(word.endsWith("ojn") || word.endsWith("ajn")){
            words.add(word.substring(0 until word.length - 1))
            words.add(word.substring(0 until word.length - 2))
        } else if(word.endsWith("oj") || word.endsWith("aj") ||
                word.endsWith("n")){
            words.add(word.substring(0 until word.length - 1))
        }

        val verbEndings = arrayListOf("as", "is", "os", "us", "anta", "inta", "onta", "ante", "ata", "ita", "ota", "u")
        for(ending in verbEndings){
            if(word.endsWith(ending) && word != ending){
                words.add(word.substring(0 until word.length - ending.length)+ "i")
            }
        }


        return words
    }

    fun languageName(langHash: HashMap<String, String>, langCode: String?): String{
        if(langCode == null)
            return ""

        if(langCode == "eo"){
            return "esperanto"
        }
        return "la " + langHash.get(langCode)
    }

    fun addTranslations(content: CharSequence,
                        translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                        langNames: HashMap<String, String>,
                        showBaseWordInTranslation: Boolean): CharSequence {
        if (translationsByLang.isEmpty()) {
            return content
        }
        var content1 = content
        val translationsTitle = SpannableString("\n\nTradukoj")
        translationsTitle.setSpan(UnderlineSpan(), 0, translationsTitle.length, 0)
        content1 = TextUtils.concat(content1, translationsTitle)
        for (langEntry in langNames) {
            val translations = translationsByLang.get(langEntry.key) ?: continue
            val lang = langEntry.value
            content1 = TextUtils.concat(
                    content1,
                    "\n\n• ", lang, "j: ",
                    translationsToString(translations, showBaseWordInTranslation))
        }
        return content1
    }

    private fun translationsToString(translations: List<TranslationResult>, showBaseWordInTranslation: Boolean): CharSequence {
        fun translationListToString(translations: List<TranslationResult>): CharSequence{
            return translations.joinToString(", ") { tr->
                if(tr.sncIndex > 0){
                    tr.sncIndex.toString() + ". " + tr.translation
                } else {
                    tr.translation
                }
            }
        }

        if(showBaseWordInTranslation){
            var content: CharSequence = ""
            val groups = translations.groupBy { it.word }
            for(key in groups.keys.sorted()){
                val value = groups[key] ?: emptyList()
                content = TextUtils.concat(content, "\n    ", key, ": ", translationListToString(value))
            }
            return content
        }
        return translationListToString(translations)
    }
}