package com.odnovolov.forgetmenot.presentation.screen.home

sealed class HomeCommand {
    object ShowNoCardsReadyForExercise : HomeCommand()
    object NavigateToExercise : HomeCommand()
    object NavigateToRepetition : HomeCommand()
    object NavigateToDeckSettings : HomeCommand()
    class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : HomeCommand()
}