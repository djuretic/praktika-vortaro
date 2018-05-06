package com.esperantajvortaroj.app

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
        val boundaries = " \n:;;.,•?!()[]{}'\""
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
}