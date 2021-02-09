package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import android.content.Intent

sealed class AddCardsEvent {
    object HelpImportFileButtonClicked : AddCardsEvent()
    object AddCardsHereButtonClicked : AddCardsEvent()
    class ReceivedContent(val intent: Intent) : AddCardsEvent()
    class DialogTextChanged(val dialogText: String) : AddCardsEvent()
    object DialogOkButtonClicked : AddCardsEvent()
    object DialogCancelButtonClicked : AddCardsEvent()
}