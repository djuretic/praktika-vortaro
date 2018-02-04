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
}