package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.ListChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertIntervalSchemeIfNotExists
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists
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
                if (change !is ListChange) return
                change.removedItemsAt.forEach { ordinal: Int ->
                    database.cardQueries.delete(deckId, ordinal)
                }
                change.movedItemsAt.forEach { (oldOrdinal: Int, newOrdinal: Int) ->
                    database.cardQueries.updateOrdinal(newOrdinal, deckId, oldOrdinal)
                }
                (change.addedItems as Map<Int, Card>).forEach { (ordinal, card) ->
                    val cardDb = card.toCardDb(deckId, ordinal)
                    database.cardQueries.insert(cardDb)
                }
            }
            Deck::exercisePreference -> {
                if (change !is PropertyValueChange) return
                val linkedExercisePreference = change.newValue as ExercisePreference
                insertExercisePreferenceIfNotExists(linkedExercisePreference)
                queries.updateExercisePreferenceId(linkedExercisePreference.id, deckId)
            }
        }
    }

    fun insertExercisePreferenceIfNotExists(exercisePreference: ExercisePreference) {
        val exists = exercisePreference.isDefault()
                || database.exercisePreferenceQueries.exists(exercisePreference.id).executeAsOne()
        if (!exists) {
            exercisePreference.intervalScheme?.let(::insertIntervalSchemeIfNotExists)
            insertPronunciationIfNotExists(exercisePreference.pronunciation)
            val exercisePreferenceDb = exercisePreference.toExercisePreferenceDb()
            database.exercisePreferenceQueries.insert(exercisePreferenceDb)
        }
    }
}