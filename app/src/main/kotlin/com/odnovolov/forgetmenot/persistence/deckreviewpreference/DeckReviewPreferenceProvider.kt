package com.odnovolov.forgetmenot.persistence.deckreviewpreference

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.persistence.DeckReviewPreferenceDb
import com.odnovolov.forgetmenot.persistence.toDeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

object DeckReviewPreferenceProvider {
    fun load(): DeckReviewPreference {
        lateinit var deckReviewPreferenceDb: DeckReviewPreferenceDb
        database.transaction {
            deckReviewPreferenceDb = database.deckReviewPreferenceQueries
                .selectAll()
                .executeAsOne()
        }
        return deckReviewPreferenceDb.toDeckReviewPreference()
    }
}