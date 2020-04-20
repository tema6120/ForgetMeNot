package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.ListChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toCardDb
import com.odnovolov.forgetmenot.persistence.toExercisePreferenceDb
import com.soywiz.klock.DateTime

class DeckPropertyChangeHandler(
    private val database: Database,
    private val exercisePreferencePropertyChangeHandler: ExercisePreferencePropertyChangeHandler
) : PropertyChangeHandler {
    override fun handle(change: PropertyChangeRegistry.Change) {
        val deckId: Long = change.propertyOwnerId
        val exists: Boolean = database.deckQueries.exists(deckId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Deck::name -> {
                if (change !is PropertyValueChange) return
                val name = change.newValue as String
                database.deckQueries.updateName(name, deckId)
            }
            Deck::lastOpenedAt -> {
                if (change !is PropertyValueChange) return
                val lastOpenedAt = change.newValue as DateTime?
                val databaseValue = lastOpenedAt?.unixMillisLong
                database.deckQueries.updateLastOpenedAt(databaseValue, deckId)
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
                database.deckQueries.updateExercisePreferenceId(linkedExercisePreference.id, deckId)
            }
        }
    }

    fun insertExercisePreferenceIfNotExists(exercisePreference: ExercisePreference) {
        val exists = exercisePreference.isDefault()
                || database.exercisePreferenceQueries.exists(exercisePreference.id).executeAsOne()
        if (!exists) {
            exercisePreference.intervalScheme
                ?.let(exercisePreferencePropertyChangeHandler::insertIntervalSchemeIfNotExists)
            exercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists(
                exercisePreference.pronunciation
            )
            exercisePreferencePropertyChangeHandler.insertSpeakPlanIfNotExists(
                exercisePreference.speakPlan
            )
            val exercisePreferenceDb = exercisePreference.toExercisePreferenceDb()
            database.exercisePreferenceQueries.insert(exercisePreferenceDb)
        }
    }
}