package com.odnovolov.forgetmenot.persistence.globalstate.writingchanges

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertIntervalSchemeIfNotExists
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists
import com.odnovolov.forgetmenot.persistence.toCardDb
import com.odnovolov.forgetmenot.persistence.toExercisePreferenceDb
import com.soywiz.klock.DateTime

object DeckPropertyChangeHandler {
    private val queries = database.deckQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        val deckId = change.propertyOwnerId
        if (!queries.exists(deckId).executeAsOne()) return
        when (change.property) {
            Deck::name -> {
                if (change !is PropertyValueChange) return
                val name = change.newValue as String
                queries.updateName(name, deckId)
            }
            Deck::lastOpenedAt -> {
                if (change !is PropertyValueChange) return
                val lastOpenedAt = change.newValue as DateTime?
                val databaseValue = lastOpenedAt?.unixMillisLong
                queries.updateLastOpenedAt(databaseValue, deckId)
            }
            Deck::cards -> {
                if (change !is CollectionChange) return
                val removedCards = change.removedItems as Collection<Card>
                removedCards.forEach { card -> database.cardQueries.delete(card.id) }
                val addedCards = change.addedItems as Collection<Card>
                insertCards(addedCards, deckId)
            }
            Deck::exercisePreference -> {
                if (change !is PropertyValueChange) return
                val linkedExercisePreference = change.newValue as ExercisePreference
                insertExercisePreferenceIfNotExists(linkedExercisePreference)
                queries.updateExercisePreferenceId(linkedExercisePreference.id, deckId)
            }
        }
    }

    fun insertCards(cards: Collection<Card>/* todo: should be List<Card> */, deckId: Long) {
        cards.forEachIndexed { index, card ->
            val cardDb = card.toCardDb(deckId, ordinal = index)
            database.cardQueries.insert(cardDb)
        }
    }

    fun insertExercisePreferenceIfNotExists(exercisePreference: ExercisePreference) {
        val exists = exercisePreference.id == ExercisePreference.Default.id
                || database.exercisePreferenceQueries.exists(exercisePreference.id).executeAsOne()
        if (!exists) {
            exercisePreference.intervalScheme?.let(::insertIntervalSchemeIfNotExists)
            insertPronunciationIfNotExists(exercisePreference.pronunciation)
            val exercisePreferenceDb = exercisePreference.toExercisePreferenceDb()
            database.exercisePreferenceQueries.insert(exercisePreferenceDb)
        }
    }
}