package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog

sealed class CardTextSizeDialogEvent {
    class TextChanged(val text: String) : CardTextSizeDialogEvent()
    object OkButtonClicked : CardTextSizeDialogEvent()
}