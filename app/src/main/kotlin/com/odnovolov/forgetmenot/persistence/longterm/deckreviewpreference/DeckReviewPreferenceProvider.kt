package com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.toEnumOrNull
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

class DeckReviewPreferenceProvider(
    private val database: Database
) : LongTermStateProvider<DeckReviewPreference> {
    override fun load(): DeckReviewPreference {
        val keyValues: Map<Long, String?> = database.keyValueQueries
            .selectValues(
                keys = listOf(
                    DbKeys.DECK_SORTING_CRITERION,
                    DbKeys.DECK_SORTING_DIRECTION,
                    DbKeys.DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE,
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val deckSortingCriterion: DeckSorting.Criterion =
            keyValues[DbKeys.DECK_SORTING_CRITERION]
                ?.toEnumOrNull<DeckSorting.Criterion>()
                ?: DeckSorting.DEFAULT_CRITERION
        val deckSortingDirection: DeckSorting.Direction =
            keyValues[DbKeys.DECK_SORTING_DIRECTION]
                ?.toEnumOrNull<DeckSorting.Direction>()
                ?: DeckSorting.DEFAULT_DIRECTION
        val deckSorting = DeckSorting(deckSortingCriterion, deckSortingDirection)
        val displayOnlyDecksAvailableForExercise: Boolean =
            keyValues[DbKeys.DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE]
                ?.toBoolean()
                ?: DeckReviewPreference.DEFAULT_DISPLAY_ONLY_DECKS_AVAILABLE_FOR_EXERCISE
        return DeckReviewPreference(0, null, deckSorting, displayOnlyDecksAvailableForExercise) // todo
    }
}