package com.odnovolov.forgetmenot.persistence.longterm.tipstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.TipState

class TipStateProvider(
    private val database: Database
) {
    fun load() {
        database.tipStateQueries.selectAll(::TipState).executeAsList()
            .forEach { loadedTipState: TipState ->
                enumValues<Tip>().find { it.state.id == loadedTipState.id }
                    ?.state?.run {
                        needToShow = loadedTipState.needToShow
                        lastShowedAt = loadedTipState.lastShowedAt
                    }
            }
    }
}