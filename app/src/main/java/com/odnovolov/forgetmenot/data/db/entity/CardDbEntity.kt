package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.odnovolov.forgetmenot.domain.entity.Card

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckDbEntity::class,
            parentColumns = ["deck_id"],
            childColumns = ["deck_id_fk"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("deck_id_fk")]
)
data class CardDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    val id: Int,

    @ColumnInfo(name = "deck_id_fk")
    val deckId: Int,

    @ColumnInfo(name = "ordinal")
    val ordinal: Int,

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "answer")
    val answer: String
) {
    fun toCard() = Card(id, ordinal, question, answer)

    companion object {
        fun fromCard(card: Card, deckId: Int) =
            CardDbEntity(card.id, deckId, card.ordinal, card.question, card.answer)
    }
}