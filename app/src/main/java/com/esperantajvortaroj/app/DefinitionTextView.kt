package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.widget.TextView

class DefinitionTextView : TextView {
    var onClickFako: (fako: String) -> Unit = {}
    var onClikStilo: (stilo: String) -> Unit = {}
    var headword: SpannableString = SpannableString("")
    var definition: SpannableString = SpannableString("")

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun setResult(definitionResult: SearchResult?,
                  translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                  langNames: HashMap<String, String>): CharSequence{
        var content : CharSequence = ""
        setTextColor(Color.BLACK)

        //textView.setTextIsSelectable(true)
        movementMethod = LinkMovementMethod.getInstance()
        if (definitionResult != null) {
            headword = SpannableString(definitionResult.word)
            headword.setSpan(StyleSpan(Typeface.BOLD), 0, definitionResult.word.length, 0)
            definition = definitionResult.formattedDefinition(context, onClickFako, onClikStilo)
            content = TextUtils.concat(headword, "\n", definition)
        }
        content = addTranslations(content, translationsByLang, langNames)
        this.text = content
        return content
    }

    private fun addTranslations(content: CharSequence,
                                translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                                langNames: HashMap<String, String>): CharSequence {
        if (translationsByLang.isEmpty()) {
            return content
        }
        var content1 = content
        val translationsTitle = SpannableString("\n\nTradukoj")
        translationsTitle.setSpan(UnderlineSpan(), 0, translationsTitle.length, 0)
        content1 = TextUtils.concat(content1, translationsTitle)
        for (langEntry in langNames) {
            val translations = translationsByLang.get(langEntry.key)
            if (translations != null) {
                val lang = langEntry.value
                content1 = TextUtils.concat(
                        content1,
                        "\n\n• ", lang, "j: ",
                        translations.joinToString(", ") { tr->
                            if(tr.sncIndex > 0){
                                tr.sncIndex.toString() + ". " + tr.translation
                            } else {
                                tr.translation
                            }
                        })
            }

        }
        return content1
    }
}