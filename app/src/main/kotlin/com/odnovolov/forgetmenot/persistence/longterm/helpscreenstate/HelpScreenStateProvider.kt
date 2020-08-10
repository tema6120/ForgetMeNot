package com.odnovolov.forgetmenot.persistence.longterm.helpscreenstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpScreenState

class HelpScreenStateProvider(
    private val database: Database
) : LongTermStateProvider<HelpScreenState> {
    override fun load(): HelpScreenState {
        lateinit var currentHelpArticle: HelpArticle
        database.transaction {
            currentHelpArticle = database.helpScreenStateQueries
                .selectAll()
                .executeAsOne()
        }
        return HelpScreenState(currentHelpArticle)
    }
}