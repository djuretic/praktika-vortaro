package com.esperantajvortaroj.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esperantajvortaroj.app.db.DatabaseHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val context = this
        doAsync {
            DatabaseHelper.getLanguagesHash(context)
            uiThread {
                val intent = Intent(context, SearchActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                context.startActivity(intent)
                context.finish()
            }

        }
    }
}