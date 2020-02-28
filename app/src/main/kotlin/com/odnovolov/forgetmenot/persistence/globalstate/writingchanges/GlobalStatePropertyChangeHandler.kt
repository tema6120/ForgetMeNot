package com.odnovolov.forgetmenot.persistence.globalstate.writingchanges

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.DeckPropertyChangeHandler.insertCards
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.DeckPropertyChangeHandler.insertExercisePreferenceIfNotExists
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertIntervalSchemeIfNotExists
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.ExercisePreferencePropertyChangeHandler.insertPronunciationIfNotExists
import com.odnovolov.forgetmenot.persistence.toDeckDb

object GlobalStatePropertyChangeHandler {
    fun handle(change: PropertyChangeRegistry.Change) {
        when (change.property) {
            GlobalState::decks -> {
                if (change !is CollectionChange) return

                val removedDecks = change.removedItems as Collection<Deck>
                removedDecks.forEach { deck: Deck ->
                    database.deckQueries.delete(deck.id)
                    deck.cards.forEach { card: Card ->
                        database.cardQueries.delete(card.id)
                    }
                }

                val addedDecks = change.addedItems as Collection<Deck>
                addedDecks.forEach { deck ->
                    val deckDb = deck.toDeckDb()
                    database.deckQueries.insert(deckDb)
                    insertCards(deck.cards, deck.id)
                    insertExercisePreferenceIfNotExists(deck.exercisePreference)
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
                    insertExercisePreferenceIfNotExists(exercisePreference)
                }
            }
            GlobalState::sharedIntervalSchemes -> {
                if (change !is CollectionChange) return

                val removedSharedIntervalSchemes = change.removedItems as Collection<IntervalScheme>
                removedSharedIntervalSchemes.forEach { intervalScheme: IntervalScheme ->
                    database.sharedIntervalSchemeQueries.delete(intervalScheme.id)
                }

                val addedSharedIntervalSchemes = change.addedItems as Collection<IntervalScheme>
                addedSharedIntervalSchemes.forEach { intervalScheme: IntervalScheme ->
                    database.sharedIntervalSchemeQueries.insert(intervalScheme.id)
                    insertIntervalSchemeIfNotExists(intervalScheme)
                }
            }
            GlobalState::sharedPronunciations -> {
                if (change !is CollectionChange) return

                val removedSharedPronunciations = change.removedItems as Collection<Pronunciation>
                removedSharedPronunciations.forEach { pronunciation: Pronunciation ->
                    database.sharedPronunciationQueries.delete(pronunciation.id)
                }

                val addedSharedPronunciations = change.addedItems as Collection<Pronunciation>
                addedSharedPronunciations.forEach { pronunciation: Pronunciation ->
                    database.sharedPronunciationQueries.insert(pronunciation.id)
                    insertPronunciationIfNotExists(pronunciation)
                }
            }
        }
    }
}