package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.content.Context
import com.esperantajvortaroj.app.db.DatabaseHelper

object DialogBuilder {
    fun showDisciplineDialog(context: Context, code: String) {
        val databaseHelper = DatabaseHelper(context)
        val description = databaseHelper.getDiscipline(code)
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setMessage(description).setTitle(code)
                .setPositiveButton(R.string.close_dialog, null)
                .create()
        dialog.show()
    }
}