package com.odnovolov.forgetmenot.persistence.longterm.initialdecksadderstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder

class InitialDecksAdderStateProvider(
    private val database: Database
) : LongTermStateProvider<InitialDecksAdder.State> {
    override fun load(): InitialDecksAdder.State {
        var areInitialDeckAdded: Boolean? = null
        database.transaction {
            areInitialDeckAdded = database.initialDecksAdderStateQueries
                .selectAll()
                .executeAsOne()
        }
        return InitialDecksAdder.State(areInitialDeckAdded!!)
    }
}