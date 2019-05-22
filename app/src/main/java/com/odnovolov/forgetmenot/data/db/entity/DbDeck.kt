package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DbDeck(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deck_id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String
)