package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.DeckReviewPreferenceDb
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

class DeckReviewPreferenceProvider(
    private val id: Long,
    private val database: Database,
    private val globalState: GlobalState
) : LongTermStateProvider<DeckReviewPreference> {
    override fun load(): DeckReviewPreference {
        val deckReviewPreferenceDb: DeckReviewPreferenceDb =
            database.deckReviewPreferenceQueries.select(id).executeAsOne()
        val deckList: DeckList? = deckReviewPreferenceDb.deckListId?.let { deckListId: Long ->
            globalState.deckLists.find { it.id == deckListId }
        }
        val deckSorting = DeckSorting(
            deckReviewPreferenceDb.deckSortingCriterion,
            deckReviewPreferenceDb.deckSortingDirection
        )
        return DeckReviewPreference(
            deckReviewPreferenceDb.id,
            deckList,
            deckSorting,
            deckReviewPreferenceDb.displayOnlyDecksAvailableForExercise
        )
    }
}