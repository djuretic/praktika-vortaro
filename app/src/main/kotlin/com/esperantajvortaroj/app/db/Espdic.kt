package com.esperantajvortaroj.app.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Espdic (
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "eo") var eo: String,
    @ColumnInfo(name = "en") var en: String
)