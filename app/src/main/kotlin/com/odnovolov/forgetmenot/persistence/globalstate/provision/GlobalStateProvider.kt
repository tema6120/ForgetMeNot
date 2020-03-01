package com.odnovolov.forgetmenot.persistence.globalstate.provision

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.entity.GlobalState

object GlobalStateProvider {
    fun load(): GlobalState {
        lateinit var tables: TablesForGlobalState
        database.transaction {
            tables = TablesForGlobalState.load()
        }
        return GlobalStateBuilder.build(tables)
    }
}