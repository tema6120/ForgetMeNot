package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.LastOpenedAt
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc

data class DeckSorting(
    val criterion: Criterion,
    val direction: Direction
) {
    enum class Criterion {
        Name,
        CreatedAt,
        LastOpenedAt
    }

    enum class Direction {
        Asc,
        Desc
    }

    companion object {
        val Default by lazy { DeckSorting(LastOpenedAt, Asc) }
    }
}