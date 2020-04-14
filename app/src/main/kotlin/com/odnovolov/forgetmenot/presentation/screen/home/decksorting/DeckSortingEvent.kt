package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

sealed class DeckSortingEvent {
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : DeckSortingEvent()
}