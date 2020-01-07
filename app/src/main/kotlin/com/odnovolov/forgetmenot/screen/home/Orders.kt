package com.odnovolov.forgetmenot.screen.home

sealed class HomeOrder {
    object ShowNoCardsReadyForExercise : HomeOrder()
    object NavigateToExercise : HomeOrder()
    object NavigateToDeckSettings : HomeOrder()
    object ShowDeckWasDeletedMessage : HomeOrder()
}