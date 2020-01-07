package com.odnovolov.forgetmenot.screen.home.decksorting

class DeckSorting(
    val criterion: Criterion,
    val direction: Direction
) {
    enum class Criterion {
        NAME,
        CREATED_AT,
        LAST_OPENED_AT
    }

    enum class Direction {
        ASC,
        DESC
    }
}