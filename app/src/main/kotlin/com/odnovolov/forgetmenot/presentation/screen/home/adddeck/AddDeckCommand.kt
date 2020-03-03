package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckadder.Parser.IllegalCardFormatException

sealed class AddDeckCommand {
    class ShowErrorMessage(val exception: IllegalCardFormatException) : AddDeckCommand()
    class SetDialogText(val text: String) : AddDeckCommand()
}