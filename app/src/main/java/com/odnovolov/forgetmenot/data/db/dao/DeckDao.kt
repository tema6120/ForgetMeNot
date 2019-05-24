package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.data.db.entity.DbCard
import com.odnovolov.forgetmenot.data.db.entity.DbDeck
import com.odnovolov.forgetmenot.data.db.toCard
import com.odnovolov.forgetmenot.data.db.toDbCard
import com.odnovolov.forgetmenot.data.db.toDbDeck
import com.odnovolov.forgetmenot.data.db.toDeck
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

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

    fun loadAll(): Observable<List<Deck>> {
        val decksObservable: Observable<List<DbDeck>> = loadAllDeckInternal()
        val cardsObservable: Observable<List<DbCard>> = loadAllCardInternal()
        return Observable.combineLatest(decksObservable,
            cardsObservable, BiFunction { dbDecks, dbCards -> combine(dbDecks, dbCards) })
    }

    @Query("SELECT * FROM decks")
    abstract fun loadAllDeckInternal(): Observable<List<DbDeck>>

    @Query("SELECT * FROM cards")
    abstract fun loadAllCardInternal(): Observable<List<DbCard>>

    private fun combine(dbDecks: List<DbDeck>, dbCards: List<DbCard>): List<Deck> {
        val groupedCards: Map<Int, List<Card>> =
            dbCards.groupBy({ dbCard: DbCard -> dbCard.deckId }, { dbCard: DbCard -> dbCard.toCard() })
        return dbDecks.map { dbDeck: DbDeck ->
            val cards: List<Card> = groupedCards[dbDeck.id] ?: emptyList()
            dbDeck.toDeck(cards)
        }
    }
}