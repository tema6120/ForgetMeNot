package com.odnovolov.forgetmenot.screen.home.decksorting

import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting

sealed class DeckSortingEvent {
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : DeckSortingEvent()
}