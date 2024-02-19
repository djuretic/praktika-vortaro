package com.esperantajvortaroj.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.esperantajvortaroj.app.databinding.ItemDefinitionBinding
import com.esperantajvortaroj.app.db.TranslationResult

class DefinitionView : RelativeLayout {
    var onClickFako: (fako: String) -> Unit = {}
    var onArticleTranslationClick: (position: Int) -> Unit = {}
    var onClickMoreOptions: (view: DefinitionView) -> Unit = {}
    var headword: SpannableString = SpannableString("")
    var definition: SpannableString = SpannableString("")
    var showMoreOptions: Boolean = true
    private var binding: ItemDefinitionBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemDefinitionBinding.inflate(inflater, this, true)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) :  super(context, attrs)

    fun setResult(definitionResult: SearchResult?,
                  translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                  langNames: HashMap<String, String>,
                  showLinks:Boolean = true,
                  showBaseWordInTranslation: Boolean = false): CharSequence{
        this.removeAllViewsInLayout()
        // val view = LayoutInflater.from(context).inflate(R.layout.item_definition, this, true)

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
            binding.moreOptionsImageView.setOnClickListener { view -> onClickMoreOptions(this) }
        } else {
            binding.moreOptionsImageView.visibility = GONE
        }

        return content
    }

    fun setTextSize(unit: Int, size: Float) {
        binding.definitionTextView.setTextSize(unit, size)
        val layoutParams = binding.moreOptionsImageView.layoutParams
        val size = TypedValue.applyDimension(unit, size, resources.displayMetrics).toInt()
        layoutParams.width = size
        layoutParams.height = size
    }

    fun setOnTouchListenerOnTextView(l: View.OnTouchListener) {
        binding.definitionTextView.setOnTouchListener(l)
    }


}