package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.LastTestedAt
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel.RawDeckPreview

data class DeckSorting(
    val criterion: Criterion,
    val direction: Direction,
    val newDecksFirst: Boolean
) {
    enum class Criterion(val selector: (RawDeckPreview) -> Comparable<*>?) {
        Name(selector = RawDeckPreview::deckName),
        CreatedAt(selector = RawDeckPreview::createdAt),
        LastTestedAt(selector = RawDeckPreview::lastTestedAt),
        FrequencyOfUse(selector = RawDeckPreview::averageLaps),
        Task(selector = RawDeckPreview::numberOfCardsReadyForExercise)
    }

    enum class Direction {
        Asc,
        Desc
    }

    companion object {
        val DEFAULT_CRITERION = LastTestedAt
        val DEFAULT_DIRECTION = Asc
    }
}