package com.odnovolov.forgetmenot.db.dao

import androidx.room.*
import com.odnovolov.forgetmenot.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck

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
    abstract fun insertInternal(deckDbEntity: DeckDbEntity): Long

    @Insert
    abstract fun insertInternal(cardDbEntity: CardDbEntity)

    // Read

    @Query("SELECT name FROM decks")
    abstract fun getAllDeckNames(): List<String>

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

    // Update

    @Transaction
    open fun updateDeck(deck: Deck) {
        val deckDbEntity = DeckDbEntity.fromDeck(deck)
        updateInternal(deckDbEntity)

        deck.cards
            .map { card: Card -> CardDbEntity.fromCard(card, deck.id) }
            .forEach { cardDbEntity: CardDbEntity -> updateInternal(cardDbEntity) }
    }

    @Update
    abstract fun updateInternal(deckDbEntity: DeckDbEntity)

    @Update
    abstract fun updateInternal(cardDbEntity: CardDbEntity)

    // Delete

    @Query("DELETE FROM decks WHERE deck_id = :deckId")
    abstract fun delete(deckId: Int): Int
}