package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler

class DeckListPropertyChangeHandler(
    database: Database
): PropertyChangeHandler {
    private val queries = database.deckListQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val deckListId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(deckListId).executeAsOne()
        if (!exists) return
        when (change.property) {
            DeckList::name -> {
                val name = change.newValue as String
                queries.updateName(name, deckListId)
            }
            DeckList::color -> {
                val color = change.newValue as Int
                queries.updateColor(color, deckListId)
            }
            DeckList::deckIds -> {
                val deckIds = change.newValue as Set<Long>
                queries.updateDeckIds(deckIds, deckListId)
            }
        }
    }
}