package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.LastTestedAt
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc

data class DeckSorting(
    val criterion: Criterion,
    val direction: Direction
) {
    enum class Criterion {
        Name,
        CreatedAt,
        LastTestedAt,
        FrequencyOfUse,
        Task
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