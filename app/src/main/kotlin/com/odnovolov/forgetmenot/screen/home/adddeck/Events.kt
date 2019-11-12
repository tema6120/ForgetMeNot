package com.odnovolov.forgetmenot.screen.home.adddeck

import com.odnovolov.forgetmenot.home.adddeck.CardPrototype
import com.odnovolov.forgetmenot.screen.home.adddeck.Parser.IllegalCardFormatException
import java.io.InputStream

sealed class AddDeckEvent {
    class ContentReceived(val inputStream: InputStream, val fileName: String?) : AddDeckEvent()
    class ParsingFinishedWithSuccess(val cardPrototypes: List<CardPrototype>) : AddDeckEvent()
    class ParsingFinishedWithError(val e: IllegalCardFormatException) : AddDeckEvent()
    class DialogTextChanged(val text: String) : AddDeckEvent()
    object PositiveDialogButtonClicked : AddDeckEvent()
    object NegativeDialogButtonClicked : AddDeckEvent()
}