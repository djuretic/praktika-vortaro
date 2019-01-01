package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.TextView
import com.esperantajvortaroj.app.db.TranslationResult

class DefinitionTextView : TextView {
    var onClickFako: (fako: String) -> Unit = {}
    var onArticleTranslationClick: (position: Int) -> Unit = {}
    var headword: SpannableString = SpannableString("")
    var definition: SpannableString = SpannableString("")

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun setResult(definitionResult: SearchResult?,
                  translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                  langNames: HashMap<String, String>,
                  showLinks:Boolean = true,
                  showBaseWordInTranslation: Boolean = false): CharSequence{
        var content : CharSequence = ""
        setTextColor(Color.BLACK)

        //textView.setTextIsSelectable(true)
        movementMethod = LinkMovementMethod.getInstance()
        if (definitionResult != null) {
            headword = SpannableString(definitionResult.word)
            headword.setSpan(StyleSpan(Typeface.BOLD), 0, definitionResult.word.length, 0)
            definition = definitionResult.formattedDefinition(if(showLinks) context else null, onClickFako)
            content = TextUtils.concat(headword, "\n", definition)
        }
        content = Utils.addTranslations(
                content, translationsByLang, langNames,
                showBaseWordInTranslation, context, onArticleTranslationClick)
        this.text = content
        return content
    }
}