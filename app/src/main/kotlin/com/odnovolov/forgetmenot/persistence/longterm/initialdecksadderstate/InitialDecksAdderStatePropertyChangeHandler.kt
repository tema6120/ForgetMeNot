package com.odnovolov.forgetmenot.persistence.longterm.initialdecksadderstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder

class InitialDecksAdderStatePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            InitialDecksAdder.State::areInitialDecksAdded -> {
                val areInitialDecksAdded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.ARE_INITIAL_DECKS_ADDED,
                    value = areInitialDecksAdded.toString()
                )
            }
        }
    }
}