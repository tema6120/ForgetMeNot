package com.odnovolov.forgetmenot.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.odnovolov.forgetmenot.data.db.toCard
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

data class DbDeck(
    @Embedded
    var deckDbRow: DeckDbRow? = null,

    @Relation(entity = CardDbRow::class, entityColumn = "deck_id", parentColumn = "deck_id")
    var cardsDbRow: List<CardDbRow>? = null
) {
    fun asDeck(): Deck {
        val cards: List<Card> = cardsDbRow!!.map { it.toCard() }
        return Deck(deckDbRow!!.id, deckDbRow!!.name, cards)
    }
}