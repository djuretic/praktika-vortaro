package com.esperantajvortaroj.app

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class SearchHistory (
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "word") var word: String
)