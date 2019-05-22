package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.data.db.entity.DbCard
import com.odnovolov.forgetmenot.data.db.entity.DbDeck
import com.odnovolov.forgetmenot.data.db.toDbCard
import com.odnovolov.forgetmenot.data.db.toDbDeck
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

@Dao
abstract class DeckDao {

    @Transaction
    open fun insert(deck: Deck): Int {
        val deckId = this.insertInternal(deck.toDbDeck()).toInt()
        deck.cards
            .map { card: Card -> card.toDbCard((deckId)) }
            .forEach { dbCard: DbCard -> insertInternal(dbCard) }
        return deckId
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertInternal(dbDeck: DbDeck): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertInternal(dbCard: DbCard)

    @Query("SELECT name FROM decks")
    abstract fun getAllDeckNames(): List<String>
}