package com.odnovolov.forgetmenot.ui.adddeck

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.odnovolov.forgetmenot.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck

@Dao
abstract class AddDeckDao {

    @Query("SELECT CASE WHEN EXISTS(SELECT * FROM decks WHERE name = :deckName) THEN 1 ELSE 0 END")
    abstract fun isDeckNameOccupied(deckName: String): Boolean

    @Transaction
    open suspend fun insertDeck(deck: Deck): Int {
        val deckDbEntity = DeckDbEntity.fromDeck(deck)
        val deckId = this.insertDeckInternal(deckDbEntity).toInt()
        val cardDbEntities = deck.cards
            .map { card: Card -> CardDbEntity.fromCard(card, deckId) }
        insertCardInternal(cardDbEntities)
        return deckId
    }

    @Insert
    abstract fun insertDeckInternal(deckDbEntity: DeckDbEntity): Long

    @Insert
    abstract fun insertCardInternal(cardDbEntities: List<CardDbEntity>)
}