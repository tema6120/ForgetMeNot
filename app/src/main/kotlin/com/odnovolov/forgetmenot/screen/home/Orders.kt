package com.odnovolov.forgetmenot.screen.home

sealed class HomeOrder {
    object ShowNoCardsReadyForExercise : HomeOrder()
    object NavigateToExercise : HomeOrder()
    object NavigateToRepetition : HomeOrder()
    object NavigateToDeckSettings : HomeOrder()
    class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : HomeOrder()
}