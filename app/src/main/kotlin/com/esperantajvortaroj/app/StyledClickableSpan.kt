package com.esperantajvortaroj.app

import androidx.core.content.ContextCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.content.Context

abstract class StyledClickableSpan(val context: Context) : ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.color = ContextCompat.getColor(context, R.color.colorPrimary)
        ds.isUnderlineText = false
    }
}
