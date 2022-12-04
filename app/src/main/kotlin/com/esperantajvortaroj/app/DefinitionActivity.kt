package com.esperantajvortaroj.app

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.esperantajvortaroj.app.databinding.ActivityDefinitionBinding
import com.esperantajvortaroj.app.db.DatabaseHelper
import com.esperantajvortaroj.app.db.TranslationResult
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.*

class DefinitionActivity : AppCompatActivity(), View.OnTouchListener {
    private var entriesList: ArrayList<Int> = arrayListOf()
    private var entryPosition = 0
    private var definitionId = 0
    private var articleId = 0

    private var definitionLinearLayout: LinearLayout? = null
    private var touchedView: View? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private var tapTooDown = false
    private var tooltipVisible = false
    private lateinit var binding: ActivityDefinitionBinding

    companion object {
        const val DEFINITION_ID = "definition_id"
        const val ARTICLE_ID = "article_id"
        const val ENTRY_POSITION = "entry_position"
        const val ENTRIES_LIST = "entries_list"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefinitionBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        binding.progressBar.visibility = View.GONE
        setSupportActionBar(binding.appToolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        gestureDetector = GestureDetectorCompat(this, object: GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) = showContextMenu(touchedView)
            override fun onSingleTapUp(e: MotionEvent?) = onSingleTap(e)
        })

        definitionId = intent.getIntExtra(DEFINITION_ID, 0)
        articleId = intent.getIntExtra(ARTICLE_ID, 0)
        val entryPosition = intent.getIntExtra(ENTRY_POSITION, 0)
        val entriesList = intent.extras?.getIntegerArrayList(ENTRIES_LIST)
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

    override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
        if(motionEvent.action == MotionEvent.ACTION_DOWN){
            touchedView = view
        }

        return gestureDetector?.onTouchEvent(motionEvent) ?: false
    }

    fun onSingleTap(motionEvent: MotionEvent?): Boolean{
        val view = touchedView
        if(view == null || view !is TextView || motionEvent == null || tooltipVisible){
            tooltipVisible = false
            return false
        }
        val layout = view.layout
        val line = layout.getLineForVertical(motionEvent.y.toInt())
        val offset = layout.getOffsetForHorizontal(line, motionEvent.x)

        val scrollViewRect = Rect()
        binding.definitionScrollView.getGlobalVisibleRect(scrollViewRect)

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constraintLayout)
        constraintSet.connect(R.id.dummyView, ConstraintSet.TOP, R.id.appToolbar, ConstraintSet.BOTTOM, motionEvent.rawY.toInt() - scrollViewRect.top)
        // api >= 17
        constraintSet.connect(R.id.dummyView, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, motionEvent.rawX.toInt() - scrollViewRect.left)
        // api < 17
        constraintSet.connect(R.id.dummyView, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, motionEvent.rawX.toInt() - scrollViewRect.left)
        constraintSet.applyTo(binding.constraintLayout)

        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = getSystemService(WindowManager::class.java).currentWindowMetrics
            tapTooDown = motionEvent.rawY >= metrics.bounds.height()*2/3
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            tapTooDown = motionEvent.rawY >= displayMetrics.heightPixels*2/3
        }

        // don't interfere with ClickableSpan
        val clickableSpans = (view.text as SpannableString).getSpans(offset, offset, ClickableSpan::class.java)
        if(clickableSpans.isNotEmpty()){
            return false
        }

        binding.progressBar.visibility = View.VISIBLE

        val word = Utils.getWholeWord(view.text, offset)
        if(word == null){
            binding.progressBar.visibility = View.GONE
        } else {
            GlobalScope.launch (Dispatchers.Default) {
                val searchResult = searchWord(word)
                withContext(Dispatchers.Main) {
                    if(searchResult == null){
                        Toast.makeText(applicationContext, "Vorto '$word' ne trovita", Toast.LENGTH_SHORT).show()
                    } else {
                        showTooltip(searchResult)
                    }
                    hideProgressBar()
                }

            }
        }
        return true
    }

    @WorkerThread
    fun searchWord(word: String): SearchResult? {
        val words = Utils.getPossibleBaseWords(word)
        val databaseHelper = DatabaseHelper(this)
        databaseHelper.use { helper ->
            for(possibleWord in words){
                val results = helper.searchWords(possibleWord, true)
                if(results.isNotEmpty()){
                    return results[0]
                }
            }
        }
        return null
    }

    fun showContextMenu(view: View?){
        registerForContextMenu(view)
        openContextMenu(view)
        unregisterForContextMenu(view)
    }

    private fun hideProgressBar(){
        binding.progressBar.visibility = View.GONE
    }

    private fun showTooltip(result: SearchResult) {
        val textSize = getTextSize() - 2

        val layout = layoutInflater.inflate(R.layout.tooltip_definition, null)
        val definitionView = layout.findViewById<DefinitionView>(R.id.tooltipDefinition)
        definitionView.showMoreOptions = false
        definitionView.setResult(result, LinkedHashMap(), HashMap(), showLinks = false)
        definitionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

        val clickView = layout.findViewById<TextView>(R.id.tooltipBottomActions)
        clickView.movementMethod = LinkMovementMethod.getInstance()
        clickView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        val showLink = SpannableString("Montri ")

        val tooltipBgColor = ContextCompat.getColor(this, R.color.colorTooltip)
        val tooltip = SimpleTooltip.Builder(this)
                .anchorView(binding.dummyView)
                .backgroundColor(tooltipBgColor)
                .arrowColor(tooltipBgColor)
                .contentView(layout, 0)
                .dismissOnInsideTouch(false)
                .gravity(if(tapTooDown) Gravity.TOP else Gravity.BOTTOM)
                .animated(false)
                .modal(true)
                .build()
        showLink.setSpan(object: StyledClickableSpan(this) {
            override fun onClick(p0: View) {
                val intent = Intent(context, DefinitionActivity::class.java)
                if(result.id > 0) {
                    intent.putExtra(DEFINITION_ID, result.id)
                    intent.putExtra(ARTICLE_ID, result.articleId)
                    intent.putExtra(ENTRY_POSITION, 0)
                    context.startActivity(intent)
                    tooltip.dismiss()
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
        definitionLinearLayout = layout
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(wordView)

        if(wordInfo.hasArticle){
            layout.addView(linkToArticleView(articleId))
        }

        with(binding.definitionScrollView){
            removeAllViews()
            addView(layout)
        }
    }

    private fun displayArticle(articleId: Int){
        val articleViews = loadArticle(articleId)
        val layout = LinearLayout(this)
        definitionLinearLayout = layout
        layout.orientation = LinearLayout.VERTICAL
        if(articleViews.isNotEmpty()){
            for(view in articleViews){
                layout.addView(view)
            }
        }

        with(binding.definitionScrollView){
            removeAllViews()
            addView(layout)
        }
    }

    private fun getTextSize(): Float{
        return PreferenceHelper.getFontSize(this).toFloat()
    }

    private fun linkToArticleView(articleId: Int): TextView {
        val textView = TextView(this)
        val text = SpannableString("\nMontri artikolon \n")
        text.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        text.setSpan(object: StyledClickableSpan(this) {
            override fun onClick(p0: View) {
                val intent = Intent(this@DefinitionActivity, DefinitionActivity::class.java)
                intent.putExtra(ARTICLE_ID, articleId)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        tooltipVisible = false
        when (item.itemId) {
            R.id.go_back_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.RESET_SEARCH, true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                return true
            }
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

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val targetView = touchedView
        if(targetView == null || targetView !is DefinitionView){
            return super.onContextItemSelected(item)
        }

        when(item.itemId){
            R.id.copyDefinition -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("difino", targetView.definition.toString()))
                return true
            }
            R.id.copyHeadword -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("kapvorto", targetView.headword.toString()))
                return true
            }
            R.id.searchPIV -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vortaro.net/#${targetView.headword}"))
                startActivity(intent)
                return true
            }
            R.id.searchKomputeko -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://komputeko.net/#${targetView.headword}"))
                startActivity(intent)
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
        val definitionView = getDefinitionView(definitionResult, translationsByLang, langNames, false)
        definitionView.setOnTouchListenerOnTextView(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(definitionView)
        return DefinitionData(layout, definitionResult?.articleId ?: 0, hasArticle)
    }

    private fun loadArticle(articleId: Int): List<View> {
        val databaseHelper = DatabaseHelper(this)
        val results = databaseHelper.articleById(articleId)
        val langNames = databaseHelper.getLanguagesHash()

        if (results.size == 1){
            return emptyList()
        }
        val translationsByLang = getTranslations(databaseHelper, results.map{ it.id })

        return results.map { res ->
            val definitionView = getDefinitionView(res, showTranslationWord = false)
            definitionView.setOnTouchListenerOnTextView(this)
            definitionView
        } + listOf(getDefinitionView(null, translationsByLang, langNames, true))
    }

    private fun getDefinitionView(definitionResult: SearchResult?,
                                  translationsByLang: LinkedHashMap<String, List<TranslationResult>> = LinkedHashMap(),
                                  langNames: HashMap<String, String> = HashMap(),
                                  showTranslationWord: Boolean): DefinitionView {
        val definitionView = DefinitionView(this)
        definitionView.onClickFako = { fako -> DialogBuilder.showDisciplineDialog(this, fako) }
        definitionView.onArticleTranslationClick = { position ->
            val view = definitionLinearLayout?.getChildAt(position)
            view?.parent?.requestChildFocus(view, view)
        }
        definitionView.showMoreOptions = definitionResult != null
        definitionView.onClickMoreOptions = { view ->
            touchedView = view
            showContextMenu(view)
        }
        definitionView.setResult(definitionResult, translationsByLang, langNames, true, showTranslationWord)
        definitionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize())
        return definitionView
    }

    private fun getTranslations(databaseHelper: DatabaseHelper, definitionId: Int): LinkedHashMap<String, List<TranslationResult>> {
        // TODO: Showing all translations mode
        val langPrefs = PreferenceHelper.getLanguagesPreference(this)

        val translationsByLang = LinkedHashMap<String, List<TranslationResult>>()
        for (lang in langPrefs) {
            val translations = databaseHelper.translationsByDefinitionId(definitionId, lang)
            if (translations.isNotEmpty()) {
                translationsByLang[lang] = translations
            }
        }
        return translationsByLang
    }

    private fun getTranslations(databaseHelper: DatabaseHelper, definitionIds: List<Int>): LinkedHashMap<String, List<TranslationResult>> {
        val langPrefs = PreferenceHelper.getLanguagesPreference(this)

        val translationsByLang = LinkedHashMap<String, List<TranslationResult>>()
        for (lang in langPrefs) {
            val translations = databaseHelper.translationsByDefinitionIds(definitionIds, lang)
            if (translations.isNotEmpty()) {
                translationsByLang[lang] = translations
            }
        }
        return translationsByLang
    }
}

data class DefinitionData(val layout: LinearLayout, val articleId: Int, val hasArticle: Boolean)