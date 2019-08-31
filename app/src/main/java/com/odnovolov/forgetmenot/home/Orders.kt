package com.odnovolov.forgetmenot.home

sealed class HomeOrder {
    object NavigateToExercise : HomeOrder()
    object NavigateToDeckSettings : HomeOrder()
    object ShowDeckWasDeletedMessage : HomeOrder()
}