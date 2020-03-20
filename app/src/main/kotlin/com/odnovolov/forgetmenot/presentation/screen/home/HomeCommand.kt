package com.odnovolov.forgetmenot.presentation.screen.home

sealed class HomeCommand {
    object ShowNoCardIsReadyForExerciseMessage : HomeCommand()
    class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : HomeCommand()
}