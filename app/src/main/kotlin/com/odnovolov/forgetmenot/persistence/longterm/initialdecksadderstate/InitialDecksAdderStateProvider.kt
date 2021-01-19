package com.odnovolov.forgetmenot.persistence.longterm.initialdecksadderstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder

class InitialDecksAdderStateProvider(
    private val database: Database
) : LongTermStateProvider<InitialDecksAdder.State> {
    override fun load(): InitialDecksAdder.State {
        return database.keyValueQueries
            .selectValue(DbKeys.ARE_INITIAL_DECKS_ADDED)
            .executeAsOneOrNull()
            ?.value?.toBoolean()?.let(InitialDecksAdder::State)
            ?: InitialDecksAdder.State()
    }
}