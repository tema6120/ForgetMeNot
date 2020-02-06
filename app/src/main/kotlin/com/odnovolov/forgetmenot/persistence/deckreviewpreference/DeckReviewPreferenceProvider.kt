package com.odnovolov.forgetmenot.persistence.deckreviewpreference

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

object DeckReviewPreferenceProvider {
    fun load(): DeckReviewPreference {
        var displayOnlyWithTasks: Boolean? = null
        database.transaction {
            displayOnlyWithTasks = database.deckReviewPreferenceQueries
                .selectAll()
                .executeAsOne()
        }
        return DeckReviewPreference(displayOnlyWithTasks!!)
    }
}