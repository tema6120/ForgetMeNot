package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

sealed class OngoingCardEditorEvent {
    object CancelButtonClicked : OngoingCardEditorEvent()
    object AcceptButtonClicked : OngoingCardEditorEvent()
}