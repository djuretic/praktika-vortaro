package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.android.synthetic.main.activity_definition.*

class DefinitionActivity : AppCompatActivity(), View.OnTouchListener {
    private var entriesList: ArrayList<Int> = arrayListOf()
    private var entryPosition = 0
    private var definitionId = 0
    private var articleId = 0

    private var touchedView: View? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private var tapTooDown = false
    private var tooltipVisible = false
    /* Used to highlight selected word when showing the tooltip */
    private var highlightSpans: Pair<SpannableString, ArrayList<CharacterStyle>>? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_definition)
        progressBar.visibility = View.GONE
        setSupportActionBar(appToolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        gestureDetector = GestureDetectorCompat(this, object: GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) = showContextMenu(touchedView)
            override fun onSingleTapUp(e: MotionEvent?) = onSingleTap(e)
        })

        definitionId = intent.getIntExtra(DEFINITION_ID, 0)
        articleId = intent.getIntExtra(ARTICLE_ID, 0)
        val entryPosition = intent.getIntExtra(ENTRY_POSITION, 0)
        val entriesList = intent.extras.getIntegerArrayList(ENTRIES_LIST)
        this.entryPosition = entryPosition
        this.entriesList = entriesList ?: arrayListOf()

        if(definitionId > 0){
            displayDefinition(definitionId)
        } else {
            displayArticle(articleId)
        }
        invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()

    }

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        if(motionEvent != null && motionEvent.action == MotionEvent.ACTION_DOWN){
            touchedView = view
        }

        return gestureDetector?.onTouchEvent(motionEvent) ?: false
    }

    fun onSingleTap(motionEvent: MotionEvent?): Boolean{
        val view = touchedView
        if(view == null || view !is TextView || motionEvent == null || tooltipVisible){
            cancelHighlightWord()
            tooltipVisible = false
            return false
        }
        val layout = view.layout
        val line = layout.getLineForVertical(motionEvent.y.toInt())
        val offset = layout.getOffsetForHorizontal(line, motionEvent.x)

        val scrollViewRect = Rect()
        definitionScrollView.getGlobalVisibleRect(scrollViewRect)

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(R.id.dummyView, ConstraintSet.TOP, R.id.appToolbar, ConstraintSet.BOTTOM, motionEvent.rawY.toInt() - scrollViewRect.top)
        // api >= 17
        constraintSet.connect(R.id.dummyView, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, motionEvent.rawX.toInt() - scrollViewRect.left)
        // api < 17
        constraintSet.connect(R.id.dummyView, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, motionEvent.rawX.toInt() - scrollViewRect.left)
        constraintSet.applyTo(constraintLayout)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        tapTooDown = motionEvent.rawY >= displayMetrics.heightPixels*2/3

        // don't interfere with ClickableSpan
        val spanText = view.text as SpannableString
        val clickableSpans = spanText.getSpans(offset, offset, ClickableSpan::class.java)
        highlightSpans = Pair(spanText, ArrayList())
        if(clickableSpans.isNotEmpty()){
            return false
        }

        progressBar.visibility = View.VISIBLE

        val wholeWordResult = Utils.getWholeWord(view.text, offset)
        if(wholeWordResult?.word == null){
            progressBar.visibility = View.GONE
        } else {
            SearchWordTask(this).execute(wholeWordResult)
        }
        return true
    }

    fun showContextMenu(view: View?){
        registerForContextMenu(view)
        openContextMenu(view)
        unregisterForContextMenu(view)
    }

    fun hideProgressBar(){
        progressBar.visibility = View.GONE
    }

    private class SearchWordTask(val context: DefinitionActivity) : AsyncTask<WholeWordResult, Void, SearchResult?>() {
        var baseWord: WholeWordResult? = null

        override fun doInBackground(vararg params: WholeWordResult?): SearchResult? {
            if(params.isEmpty()) return null
            val word = params[0]?.word ?: return null
            baseWord = params[0]

            val words = Utils.getPossibleBaseWords(word)
            val databaseHelper = DatabaseHelper(context)
            try{
                for(possibleWord in words){
                    val results = databaseHelper.searchWords(possibleWord, true)
                    if(results.isNotEmpty()){
                        return results[0]
                    }
                }
            } finally {
                databaseHelper.close()
            }
            return null
        }

        override fun onPostExecute(result: SearchResult?) {
            if(result == null){
                Toast.makeText(context, "Vorto '${baseWord?.word}' ne trovita", Toast.LENGTH_SHORT).show()
            } else {
                context.showTooltip(baseWord, result)
            }
            context.hideProgressBar()
        }
    }

    private fun highlightWord(wholeWordResult: WholeWordResult){
        var highSpan = highlightSpans
        if(highSpan == null) return
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        val backColorSpan = BackgroundColorSpan(color)
        val foreColorSpan = ForegroundColorSpan(Color.WHITE)
        highSpan.second.add(backColorSpan)
        highSpan.second.add(foreColorSpan)
        highSpan.first.setSpan(backColorSpan, wholeWordResult.start, wholeWordResult.end + 1, 0)
        highSpan.first.setSpan(foreColorSpan, wholeWordResult.start, wholeWordResult.end + 1, 0)
    }

    private fun cancelHighlightWord(){
        var highSpan = highlightSpans
        if(highSpan == null) return
        highSpan.second.forEach { highSpan.first.removeSpan(it) }
    }

    private fun showTooltip(wholeWordResult: WholeWordResult?, result: SearchResult) {
        if(wholeWordResult == null) return
        highlightWord(wholeWordResult)
        val textSize = getTextSize() - 2

        val layout = layoutInflater.inflate(R.layout.tooltip_definition, null)
        val textView = layout.findViewById<DefinitionTextView>(R.id.tooltipDefinition)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        textView.setResult(result, LinkedHashMap(), HashMap(), showLinks = false)

        val clickView = layout.findViewById<TextView>(R.id.tooltipBottomActions)
        clickView.movementMethod = LinkMovementMethod.getInstance()
        clickView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        val showLink = SpannableString("Montri ")

        val tooltipBgColor = ContextCompat.getColor(this, R.color.colorTooltip)
        val tooltip = SimpleTooltip.Builder(this)
                .anchorView(dummyView)
                .backgroundColor(tooltipBgColor)
                .arrowColor(tooltipBgColor)
                .contentView(layout, 0)
                .dismissOnInsideTouch(false)
                .gravity(if(tapTooDown) Gravity.TOP else Gravity.BOTTOM)
                .animated(false)
                .modal(true)
                .build()
        showLink.setSpan(object: StyledClickableSpan(this) {
            override fun onClick(p0: View?) {
                val intent = Intent(context, DefinitionActivity::class.java)
                if(result.id > 0) {
                    intent.putExtra(DefinitionActivity.DEFINITION_ID, result.id)
                    intent.putExtra(DefinitionActivity.ARTICLE_ID, result.articleId)
                    intent.putExtra(DefinitionActivity.ENTRY_POSITION, 0)
                    context.startActivity(intent)
                    tooltip.dismiss()
                    cancelHighlightWord()
                    tooltipVisible = false
                }
            }
        }, 0, showLink.length - 1, 0)
        showLink.setSpan(StyleSpan(Typeface.BOLD), 0, showLink.length, 0)
        clickView.text = showLink
        tooltipVisible = true
        tooltip.show()
    }

    private fun displayDefinition(definitionId: Int){
        val wordInfo = loadDefinition(definitionId)
        val wordView = wordInfo.layout
        val articleId = wordInfo.articleId

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(wordView)

        if(wordInfo.hasArticle){
            layout.addView(linkToArticleView(articleId))
        }

        with(definitionScrollView){
            removeAllViews()
            addView(layout)
        }
    }

    private fun displayArticle(articleId: Int){
        val articleViews = loadArticle(articleId)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        if(articleViews.isNotEmpty()){
            for(view in articleViews){
                layout.addView(view)
            }
        }

        with(definitionScrollView){
            removeAllViews()
            addView(layout)
        }
    }

    private fun getTextSize(): Float{
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPrefs.getInt(SettingsActivity.FONT_SIZE, SettingsActivity.DEFAULT_FONT_SIZE).toFloat()
    }

    private fun linkToArticleView(articleId: Int): TextView {
        val textView = TextView(this)
        val text = SpannableString("\nMontri artikolon \n")
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        text.setSpan(object: StyledClickableSpan(this) {
            override fun onClick(p0: View?) {
                val intent = Intent(this@DefinitionActivity, DefinitionActivity::class.java)
                intent.putExtra(DefinitionActivity.ARTICLE_ID, articleId)
                startActivity(intent)
            }

        }, 1, text.length - 2, 0)
        textView.text = text
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize())
        return textView
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.entry_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(menu != null){
            val prevButton = menu.findItem(R.id.prev_entry)
            val nextButton = menu.findItem(R.id.next_entry)

            prevButton.isEnabled = entryPosition != 0
            nextButton.isEnabled = entryPosition < entriesList.size - 1
            prevButton.icon.alpha = if(prevButton.isEnabled) 255 else 130
            nextButton.icon.alpha = if(nextButton.isEnabled) 255 else 130
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        tooltipVisible = false
        cancelHighlightWord()
        when (item?.itemId) {
            R.id.prev_entry -> {
                if(entryPosition == 0){
                    return true
                }
                entryPosition--
                displayDefinition(entriesList[entryPosition])
                invalidateOptionsMenu()
                return true
            }
            R.id.next_entry -> {
                if(entryPosition >= entriesList.size - 1){
                    return true
                }
                entryPosition++
                displayDefinition(entriesList[entryPosition])
                invalidateOptionsMenu()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.definition_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val targetView = touchedView
        if(targetView == null || targetView !is DefinitionTextView){
            return super.onContextItemSelected(item)
        }

        when(item?.itemId){
            R.id.copyDefinition -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText("difino", targetView.definition.toString())
                return true
            }
            R.id.copyHeadword -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText("kapvorto", targetView.headword.toString())
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun loadDefinition(definitionId: Int): DefinitionData {
        val databaseHelper = DatabaseHelper(this)
        val definitionResult = databaseHelper.definitionById(definitionId)
        var hasArticle = false
        if(definitionResult?.articleId != null) {
           hasArticle =   databaseHelper.getArticleCountDefinitions(definitionResult.articleId) > 1
        }

        val translationsByLang = getTranslations(databaseHelper, definitionId)
        val langNames = databaseHelper.getLanguagesHash()
        databaseHelper.close()
        val textView = getDefinitionTextView(definitionResult, translationsByLang, langNames)
        textView.setOnTouchListener(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(textView)
        return DefinitionData(layout, definitionResult?.articleId ?: 0, hasArticle)
    }

    private fun loadArticle(articleId: Int): List<View> {
        val databaseHelper = DatabaseHelper(this)
        val results = databaseHelper.articleById(articleId)

        if (results.size == 1){
            return emptyList()
        }

        return results.map { res ->
            val translationsByLang = getTranslations(databaseHelper, definitionId)
            val langNames = databaseHelper.getLanguagesHash()
            val textView = getDefinitionTextView(res, translationsByLang, langNames)
            textView.setOnTouchListener(this)
            textView
        }
    }

    private fun getDefinitionTextView(definitionResult: SearchResult?,
                                      translationsByLang: LinkedHashMap<String, List<TranslationResult>>,
                                      langNames: HashMap<String, String>): DefinitionTextView {
        val textView = DefinitionTextView(this)
        textView.onClickFako = { fako -> DialogBuilder.showDisciplineDialog(this, fako) }
        textView.onClikStilo = { stilo -> DialogBuilder.showStyleDialog(this, stilo)}
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize())
        textView.setResult(definitionResult, translationsByLang, langNames)
        return textView
    }

    private fun getTranslations(databaseHelper: DatabaseHelper, definitionId: Int): LinkedHashMap<String, List<TranslationResult>> {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val langPrefs = sharedPref.getStringSet(SettingsActivity.KEY_LANGUAGES_PREFERENCE, null)

        val translationsByLang = LinkedHashMap<String, List<TranslationResult>>()
        for (lang in langPrefs) {
            val translations = databaseHelper.translationsByDefinitionId(definitionId, lang)
            if (translations.isNotEmpty()) {
                translationsByLang.put(lang, translations)
            }
        }
        return translationsByLang
    }

    companion object {
        const val DEFINITION_ID = "definition_id"
        const val ARTICLE_ID = "article_id"
        const val ENTRY_POSITION = "entry_position"
        const val ENTRIES_LIST = "entries_list"
    }
}

data class DefinitionData(val layout: LinearLayout, val articleId: Int, val hasArticle: Boolean)