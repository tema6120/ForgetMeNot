package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog

sealed class CardTexOpacityDialogEvent {
    class TextOpacityWasSelected(val textOpacity: Float) : CardTexOpacityDialogEvent()
    object OkButtonClicked : CardTexOpacityDialogEvent()
}