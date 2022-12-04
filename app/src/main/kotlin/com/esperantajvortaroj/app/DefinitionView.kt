package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.esperantajvortaroj.app.databinding.ItemDefinitionBinding
import com.esperantajvortaroj.app.db.TranslationResult

class DefinitionView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle) {
    var onClickFako: (fako: String) -> Unit = {}
    var onArticleTranslationClick: (position: Int) -> Unit = {}
    var onClickMoreOptions: (view: DefinitionView) -> Unit = {}
    var headword: SpannableString = SpannableString("")
    var definition: SpannableString = SpannableString("")
    var showMoreOptions: Boolean = true
    private val binding = ItemDefinitionBinding.inflate(LayoutInflater.from(context), this, true)

    fun setResult(definitionResult: SearchResult?,
                  translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                  langNames: HashMap<String, String>,
                  showLinks:Boolean = true,
                  showBaseWordInTranslation: Boolean = false): CharSequence{
        this.removeAllViewsInLayout()

        var content : CharSequence = ""
        val textView = binding.definitionTextView

        //textView.setTextIsSelectable(true)
        textView.movementMethod = LinkMovementMethod.getInstance()
        if (definitionResult != null) {
            headword = SpannableString(definitionResult.word)
            headword.setSpan(StyleSpan(Typeface.BOLD), 0, definitionResult.word.length, 0)
            definition = definitionResult.formattedDefinition(if(showLinks) context else null, onClickFako)
            content = TextUtils.concat(headword, "\n", definition)
        }
        content = Utils.addTranslations(
                content, translationsByLang, langNames,
                showBaseWordInTranslation, context, onArticleTranslationClick)
        textView.text = content

        if (showMoreOptions) {
            binding.moreOptionsImageView.setOnClickListener { onClickMoreOptions(this) }
        } else {
            binding.moreOptionsImageView.visibility = GONE
        }
        return content
    }

    fun setTextSize(unit: Int, size: Float) {
        binding.definitionTextView.setTextSize(unit, size)
        val layoutParams = binding.moreOptionsImageView.layoutParams
        val newSize = TypedValue.applyDimension(unit, size, resources.displayMetrics).toInt()
        layoutParams.width = newSize
        layoutParams.height = newSize
    }

    fun setOnTouchListenerOnTextView(l: OnTouchListener) {
        binding.definitionTextView.setOnTouchListener(l)
    }


}