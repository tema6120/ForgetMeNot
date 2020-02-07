package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

sealed class DeckSortingCommand {
    object DismissBottomSheet : DeckSortingCommand()
}