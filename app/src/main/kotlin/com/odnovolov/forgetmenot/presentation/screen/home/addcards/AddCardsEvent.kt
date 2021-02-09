package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import android.content.Intent

sealed class AddCardsEvent {
    class ReceivedContent(val intent: Intent) : AddCardsEvent()
    object HelpImportFileButtonClicked : AddCardsEvent()
    object CreateNewDeckButtonClicked : AddCardsEvent()
}