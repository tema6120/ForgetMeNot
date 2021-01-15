package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import java.io.InputStream

sealed class AddDeckEvent {
    object HelpImportFileButtonClicked : AddDeckEvent()
    object AddCardsHereButtonClicked : AddDeckEvent()
    class ContentReceived(val inputStream: InputStream, val fileName: String?) : AddDeckEvent()
    class DialogTextChanged(val dialogText: String) : AddDeckEvent()
    object DialogOkButtonClicked : AddDeckEvent()
    object DialogCancelButtonClicked : AddDeckEvent()
}