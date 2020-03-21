package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider

object GlobalStateProvider : LongTermStateProvider<GlobalState> {
    override fun load(): GlobalState {
        lateinit var tables: TablesForGlobalState
        database.transaction {
            tables = TablesForGlobalState.load()
        }
        return GlobalStateBuilder.build(tables)
    }
}