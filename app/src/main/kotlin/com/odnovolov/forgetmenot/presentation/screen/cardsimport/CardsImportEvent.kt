package com.odnovolov.forgetmenot.presentation.screen.cardsimport

sealed class CardsImportEvent {
    class CardsFileWasOpened(val cardsFileId: Long) : CardsImportEvent()
    object PreviousButtonClicked : CardsImportEvent()
    object NextButtonClicked : CardsImportEvent()
    object SkipButtonClicked : CardsImportEvent()
    object CancelButtonClicked : CardsImportEvent()
    object DoneButtonClicked : CardsImportEvent()
    object FixErrorsButtonClicked : CardsImportEvent()
    object ImportIgnoringErrorsButtonClicked : CardsImportEvent()
    object BackButtonClicked : CardsImportEvent()
    object UserConfirmedExit : CardsImportEvent()
}