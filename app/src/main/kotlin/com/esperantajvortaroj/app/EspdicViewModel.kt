package com.esperantajvortaroj.app

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.esperantajvortaroj.app.db.Espdic
import com.esperantajvortaroj.app.db.EspdicDao
import com.esperantajvortaroj.app.db.EspdicDatabase

class EspdicViewModel(application: Application) : AndroidViewModel(application), SearchableViewModel<Espdic> {
    private val espdicDao: EspdicDao = EspdicDatabase.getInstance(application).espdicDao()

    override fun search(word: String, language: String) : List<Espdic> {
        if (language == "eo") return espdicDao.getEo(word)
        else if (language == "en") return espdicDao.getEn(word)
        return emptyList()
    }
}