package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.data.db.entity.CardDbRow
import com.odnovolov.forgetmenot.data.db.entity.DbDeck
import com.odnovolov.forgetmenot.data.db.entity.DeckDbRow
import com.odnovolov.forgetmenot.data.db.toDbCard
import com.odnovolov.forgetmenot.data.db.toDbDeck
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import io.reactivex.Observable

@Dao
abstract class DeckDao {

    @Transaction
    open fun insert(deck: Deck): Int {
        val deckId = this.insertInternal(deck.toDbDeck()).toInt()
        deck.cards
            .map { card: Card -> card.toDbCard((deckId)) }
            .forEach { cardDbRow: CardDbRow -> insertInternal(cardDbRow) }
        return deckId
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertInternal(deckDbRow: DeckDbRow): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertInternal(cardDbRow: CardDbRow)

    @Query("SELECT name FROM decks")
    abstract fun getAllDeckNames(): List<String>

    fun loadAll(): Observable<List<Deck>> {
        return loadAllInternal()
            .map { dbDecks: List<DbDeck> ->
                dbDecks.map { dbDeck -> dbDeck.asDeck() }
            }
    }

    @Transaction
    @Query("SELECT * from decks")
    abstract fun loadAllInternal(): Observable<List<DbDeck>>

    @Transaction
    open fun delete(deckId: Int) {
        deleteCardsInternal(deckId)
        deleteDeckInternal(deckId)
    }

    @Query("DELETE FROM cards WHERE deck_id = :deckId")
    abstract fun deleteCardsInternal(deckId: Int)

    @Query("DELETE FROM decks WHERE deck_id = :deckId")
    abstract fun deleteDeckInternal(deckId: Int)
}