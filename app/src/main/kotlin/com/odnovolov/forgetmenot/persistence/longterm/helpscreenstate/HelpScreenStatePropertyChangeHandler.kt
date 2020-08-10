package com.odnovolov.forgetmenot.persistence.longterm.helpscreenstate

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpScreenState

class HelpScreenStatePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.helpScreenStateQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            HelpScreenState::currentArticle -> {
                val currentHelpArticle = change.newValue as HelpArticle
                queries.updateCurrentHelpArtice(currentHelpArticle)
            }
        }
    }
}