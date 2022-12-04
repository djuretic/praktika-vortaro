package com.esperantajvortaroj.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.esperantajvortaroj.app.db.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        AppCompatDelegate.setDefaultNightMode(PreferenceHelper.getNightMode(this))

        val context = this
        GlobalScope.async(Dispatchers.Default) {
            DatabaseHelper.getLanguagesHash(context)
            withContext(Dispatchers.Main) {
                val intent = Intent(context, SearchActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                context.startActivity(intent)
                context.finish()
            }

        }
    }
}