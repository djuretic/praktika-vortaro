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
        if (text[position] in boundaries) return null
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
}