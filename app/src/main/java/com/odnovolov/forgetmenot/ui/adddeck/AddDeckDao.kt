package com.odnovolov.forgetmenot.ui.adddeck

import androidx.lifecycle.LiveData
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

    @Query("SELECT name FROM decks")
    abstract fun getAllDeckNames(): LiveData<List<String>>

    @Transaction
    open fun insertDeck(deck: Deck): Int {
        val deckDbEntity = DeckDbEntity.fromDeck(deck)
        val deckId = this.insertInternal(deckDbEntity).toInt()
        deck.cards
            .map { card: Card -> CardDbEntity.fromCard(card, deckId) }
            .forEach { cardDbEntity: CardDbEntity -> insertInternal(cardDbEntity) }
        return deckId
    }

    @Insert
    abstract fun insertInternal(deckDbEntity: DeckDbEntity): Long

    @Insert
    abstract fun insertInternal(cardDbEntity: CardDbEntity)
}