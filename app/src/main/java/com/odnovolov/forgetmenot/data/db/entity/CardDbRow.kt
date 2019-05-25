package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.*

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckDbRow::class,
            parentColumns = ["deck_id"],
            childColumns = ["deck_id"]
        )
    ],
    indices = [Index("deck_id")]
)
data class CardDbRow (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    val id: Int,

    @ColumnInfo(name = "deck_id")
    val deckId: Int,

    @ColumnInfo(name = "ordinal")
    val ordinal: Int,

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "answer")
    val answer: String
)