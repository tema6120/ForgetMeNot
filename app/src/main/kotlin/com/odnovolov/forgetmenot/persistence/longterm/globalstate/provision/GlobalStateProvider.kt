package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider

class GlobalStateProvider(private val database: Database) : LongTermStateProvider<GlobalState> {
    override fun load(): GlobalState {
        lateinit var tables: TablesForGlobalState
        database.transaction {
            tables = TablesForGlobalState.load(database)
        }
        return GlobalStateBuilder.build(tables)
    }
}