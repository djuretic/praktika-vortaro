package com.esperantajvortaroj.app


interface SearchableViewModel<T> {
    fun search(word: String, language: String) : List<T>
}

