package com.odnovolov.forgetmenot.persistence.globalstate.writingchanges

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.DeckPropertyChangeHandler.insertCards
import com.odnovolov.forgetmenot.persistence.globalstate.writingchanges.DeckPropertyChangeHandler.insertExercisePreferenceIfNotExists
import com.odnovolov.forgetmenot.persistence.toDeckDb

object GlobalStatePropertyChangeHandler {
    fun handle(change: PropertyChangeRegistry.Change) {
        when (change.property) {
            GlobalState::decks -> {
                if (change !is CollectionChange) return

                val removedDecks = change.removedItems as Collection<Deck>
                removedDecks.forEach { deck -> database.deckQueries.delete(deck.id) }

                val addedDecks = change.addedItems as Collection<Deck>
                addedDecks.forEach { deck ->
                    val deckDb = deck.toDeckDb()
                    database.deckQueries.insert(deckDb)
                    insertCards(deck.cards, deck.id)
                    insertExercisePreferenceIfNotExists(deck.exercisePreference)
                }
            }
        }
    }
}