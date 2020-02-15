package com.odnovolov.forgetmenot.presentation.screen.home

sealed class HomeCommand {
    object ShowNoCardIsReadyForExerciseMessage : HomeCommand()
    object NavigateToRepetition : HomeCommand()
    object NavigateToDeckSettings : HomeCommand()
    class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : HomeCommand()
}