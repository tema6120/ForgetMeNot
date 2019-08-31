package com.odnovolov.forgetmenot.home.decksorting

sealed class DeckSortingEvent {
    class SortByButtonClicked(val deckSorting: DeckSorting) : DeckSortingEvent()
}