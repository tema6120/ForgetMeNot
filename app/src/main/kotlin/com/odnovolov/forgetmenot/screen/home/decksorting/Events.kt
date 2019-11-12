package com.odnovolov.forgetmenot.screen.home.decksorting

sealed class DeckSortingEvent {
    class SortByButtonClicked(val deckSorting: DeckSorting) : DeckSortingEvent()
}