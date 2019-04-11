package com.esperantajvortaroj.app

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.esperantajvortaroj.app.db.*

class AulexViewModel(application: Application) : AndroidViewModel(application) {
    private val aulexDao: AulexDao = AulexDatabase.getInstance(application).aulexDao()

    fun search(word: String, language: String) : List<Aulex> {
        if (language == "eo") return aulexDao.getEo(word)
        else if (language == "es") return aulexDao.getEs(word)
        return emptyList()
    }

    fun definitionById(id: Int): Aulex {
        return aulexDao.getById(id)
    }
}