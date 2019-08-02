package com.odnovolov.forgetmenot.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.entity.ExercisePreference
import java.util.*

@Entity(tableName = "decks")
data class DeckDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deck_id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Calendar,

    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Calendar?,

    @ColumnInfo(name = "random_order")
    val randomOrder: Boolean

) {
    fun toDeck(cards: List<Card>) = Deck(
        id,
        name,
        cards,
        createdAt,
        lastOpenedAt,
        ExercisePreference(randomOrder)
    )

    companion object {
        fun fromDeck(deck: Deck) = DeckDbEntity(
            deck.id,
            deck.name,
            deck.createdAt,
            deck.lastOpenedAt,
            deck.exercisePreference.randomOrder
        )
    }
}