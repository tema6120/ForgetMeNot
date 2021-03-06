package com.odnovolov.forgetmenot.presentation.screen.home

sealed class SelectionToolbarTitle {
    data class NumberOfSelectedDecks(val numberOfSelectedDecks: Int) : SelectionToolbarTitle()
    object ChooseDecksToPlay : SelectionToolbarTitle()
    object ChooseDecksForExercise : SelectionToolbarTitle()
    data class NumberOfSelectedCards(val numberOfSelectedCards: Int) : SelectionToolbarTitle()
}