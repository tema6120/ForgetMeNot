package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

sealed class AddDeckCommand {
    class ShowErrorMessage(val text: String) : AddDeckCommand()
    class SetDialogText(val text: String) : AddDeckCommand()
    object NavigateToDeckSettings : AddDeckCommand()
}