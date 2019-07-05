package com.odnovolov.forgetmenot.data.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.data.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.data.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import io.reactivex.Observable

@Dao
abstract class DeckDao {

    // Create

    @Transaction
    open fun insert(deck: Deck): Int {
        val deckDbEntity = DeckDbEntity.fromDeck(deck)
        val deckId = this.insertInternal(deckDbEntity).toInt()
        deck.cards
            .map { card: Card -> CardDbEntity.fromCard(card, deckId) }
            .forEach { cardDbEntity: CardDbEntity -> insertInternal(cardDbEntity) }
        return deckId
    }

    @Insert
    abstract fun insertInternal(deckDbRow: DeckDbEntity): Long

    @Insert
    abstract fun insertInternal(cardDbRow: CardDbEntity)

    // Read

    @Query("SELECT name FROM decks")
    abstract fun getAllDeckNames(): List<String>

    fun observeAll(): Observable<List<Deck>> {
        return observeAllInternal()
            .map { queryDataList: List<DeckQueryData> ->
                queryDataList.map { deckQueryData -> deckQueryData.toDeck() }
            }
    }

    @Transaction
    @Query("SELECT * from decks")
    abstract fun observeAllInternal(): Observable<List<DeckQueryData>>

    fun load(deckId: Int): Deck {
        return loadInternal(deckId).toDeck()
    }

    @Transaction
    @Query("SELECT * from decks WHERE deck_id = :deckId")
    abstract fun loadInternal(deckId: Int): DeckQueryData

    class DeckQueryData {
        @Embedded
        lateinit var deckDbEntity: DeckDbEntity

        @Relation(entity = CardDbEntity::class, entityColumn = "deck_id_fk", parentColumn = "deck_id")
        lateinit var cardDbEntities: List<CardDbEntity>

        fun toDeck(): Deck {
            val cards: List<Card> = cardDbEntities.map { it.toCard() }
            return deckDbEntity.toDeck(cards)
        }
    }

    // Delete

    @Query("DELETE FROM decks WHERE deck_id = :deckId")
    abstract fun delete(deckId: Int): Int
}