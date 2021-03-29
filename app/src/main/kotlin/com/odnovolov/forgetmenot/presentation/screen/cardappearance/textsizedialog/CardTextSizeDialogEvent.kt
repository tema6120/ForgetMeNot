package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog

sealed class CardTextSizeDialogEvent {
    class TextSizeDialogTextChanged(val text: String) : CardTextSizeDialogEvent()
    object TextSizeDialogOkButtonClicked : CardTextSizeDialogEvent()
}