package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.room.*
import com.odnovolov.forgetmenot.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.db.entity.ExerciseCardDbEntity
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.entity.ExerciseCard
import java.util.*

@Dao
abstract class ExerciseCreatorDao {

    fun getDeck(deckId: Int): Deck {
        val roughDeck = getDeckInternal(deckId)
        val cards: List<Card> = roughDeck.cardDbEntities.map { it.toCard() }
        return roughDeck.deckDbEntity.toDeck(cards)
    }

    @Transaction
    @Query("SELECT * FROM decks WHERE deck_id = :deckId")
    abstract fun getDeckInternal(deckId: Int): RoughDeck

    class RoughDeck {
        @Embedded
        lateinit var deckDbEntity: DeckDbEntity

        @Relation(entity = CardDbEntity::class, entityColumn = "deck_id_fk", parentColumn = "deck_id")
        lateinit var cardDbEntities: List<CardDbEntity>
    }

    @Query("DELETE FROM exercise_cards")
    abstract fun deleteAllExerciseCards()

    fun insertExerciseCards(exerciseCards: List<ExerciseCard>) {
        val exerciseCardDbEntities = exerciseCards.map { ExerciseCardDbEntity.fromExerciseCard(it) }
        insertInternal(exerciseCardDbEntities)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertInternal(exerciseCards: List<ExerciseCardDbEntity>)

    @Query("UPDATE decks SET last_opened_at = :lastOpenedAt WHERE deck_id = :deckId")
    abstract fun setLastOpenedAt(lastOpenedAt: Calendar, deckId: Int)

}