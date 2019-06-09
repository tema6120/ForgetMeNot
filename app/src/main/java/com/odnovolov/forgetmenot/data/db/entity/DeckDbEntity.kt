package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

@Entity(tableName = "decks")
data class DeckDbEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deck_id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String
) {
    fun toDeck(cards: List<Card>) = Deck(id, name, cards)

    companion object {
        fun fromDeck(deck: Deck) = DeckDbEntity(deck.id, deck.name)
    }
}