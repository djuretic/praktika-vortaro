package com.esperantajvortaroj.app.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchHistory (
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "word") var word: String
)