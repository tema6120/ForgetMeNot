package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toCardDb
import com.odnovolov.forgetmenot.persistence.toDeckDb

class GlobalStatePropertyChangeHandler(
    private val database: Database,
    private val deckPropertyChangeHandler: DeckPropertyChangeHandler
) : PropertyChangeHandler {
    override fun handle(change: PropertyChangeRegistry.Change) {
        when (change.property) {
            GlobalState::decks -> {
                if (change !is CollectionChange) return

                val removedDecks = change.removedItems as Collection<Deck>
                removedDecks.forEach { deck: Deck ->
                    database.deckQueries.delete(deck.id)
                }

                val addedDecks = change.addedItems as Collection<Deck>
                addedDecks.forEach { deck ->
                    val deckDb = deck.toDeckDb()
                    database.deckQueries.insert(deckDb)
                    deck.cards.mapIndexed { index, card -> card.toCardDb(deck.id, ordinal = index) }
                        .forEach(database.cardQueries::insert)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(
                        deck.exercisePreference
                    )
                }
            }
            GlobalState::sharedExercisePreferences -> {
                if (change !is CollectionChange) return

                val removedSharedExercisePreferences =
                    change.removedItems as Collection<ExercisePreference>
                removedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.delete(exercisePreference.id)
                }

                val addedSharedExercisePreferences =
                    change.addedItems as Collection<ExercisePreference>
                addedSharedExercisePreferences.forEach { exercisePreference: ExercisePreference ->
                    database.sharedExercisePreferenceQueries.insert(exercisePreference.id)
                    deckPropertyChangeHandler.insertExercisePreferenceIfNotExists(exercisePreference)
                }
            }
            GlobalState::isWalkingModeEnabled -> {
                if (change !is PropertyValueChange) return
                val isWalkingModeEnabled = change.newValue as Boolean
                database.keyValueQueries.replace(
                    key = DbKeys.IS_WALKING_MODE_ENABLED,
                    value = isWalkingModeEnabled.toString()
                )
            }
            GlobalState::numberOfLapsInPlayer -> {
                if (change !is PropertyValueChange) return
                // todo
            }
        }
    }
}