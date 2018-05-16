package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View

data class SearchResult(
        val id: Int, val articleId: Int?, val word: String, val definition: String, val format: StringFormat?) {

    fun formattedDefinition(context: Context?,
                            fakoCallback: (fako: String) -> Unit = {},
                            stiloCallback: (stilo: String) -> Unit = {}): SpannableString {
        val def = SpannableString(definition)
        if(format != null){
            applyFormat(def, format.bold, { arrayOf(StyleSpan(Typeface.BOLD))} )
            applyFormat(def, format.italic, {arrayOf(StyleSpan(Typeface.ITALIC))})
            applyFormat(def, format.ekz, {arrayOf(StyleSpan(Typeface.ITALIC), ForegroundColorSpan(Color.GRAY))})
            applyFormat(def, format.tld, { arrayOf(UnderlineSpan())} )

            if(context != null) {
                for (pair in format.fako) {
                    def.setSpan(object : StyledClickableSpan(context) {
                        override fun onClick(view: View?) {
                            fakoCallback(def.substring(pair.first..pair.second))
                        }
                    }, pair.first, pair.second, 0)
                }

                for (pair in format.stilo) {
                    def.setSpan(object : StyledClickableSpan(context) {
                        override fun onClick(view: View?) {
                            stiloCallback(def.substring(pair.first..pair.second))
                        }
                    }, pair.first, pair.second, 0)
                }
            }
        }
        return def
    }

    private fun applyFormat(def: SpannableString, format: List<Pair<Int, Int>>, styleArr: () -> Array<CharacterStyle>){
        if (format.isNotEmpty()) {
            for (pair in format) {
                for(style in styleArr())
                    def.setSpan(style, pair.first, pair.second, 0)
            }
        }
    }
}
