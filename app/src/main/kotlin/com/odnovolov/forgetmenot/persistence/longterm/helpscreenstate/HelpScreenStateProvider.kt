package com.odnovolov.forgetmenot.persistence.longterm.helpscreenstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState

class HelpScreenStateProvider(
    private val database: Database
) : LongTermStateProvider<HelpArticleScreenState> {
    override fun load(): HelpArticleScreenState {
        lateinit var currentHelpArticle: HelpArticle
        database.transaction {
            currentHelpArticle = database.helpScreenStateQueries
                .selectAll()
                .executeAsOne()
        }
        return HelpArticleScreenState(currentHelpArticle)
    }
}