package com.esperantajvortaroj.app

import android.app.AlertDialog
import android.content.Context

class DialogBuilder {
    companion object {
        fun showDisciplineDialog(context: Context, code: String) {
            val databaseHelper = DatabaseHelper(context)
            val description = databaseHelper.getDiscipline(code)
            val builder = AlertDialog.Builder(context)
            val dialog = builder.setMessage(description).setTitle(code)
                    .setPositiveButton(R.string.close_dialog, null)
                    .create()
            dialog.show()
        }

        fun showStyleDialog(context: Context, code: String) {
            val styles = hashMapOf(
                    "FRAZ" to "frazaĵo",
                    "FIG" to "figure",
                    "VULG" to "vulgare",
                    "RAR" to "malofte",
                    "POE" to "poezie",
                    "ARK" to "arkaismo",
                    "EVI" to "evitinde",
                    "KOMUNE" to "komune",
                    "NEO" to "neologismo"
            )
            val description = styles.get(code.trim()) ?: code
            val builder = AlertDialog.Builder(context)
            val dialog = builder.setMessage(description).setTitle(code)
                    .setPositiveButton(R.string.close_dialog, null)
                    .create()
            dialog.show()
        }
    }
}