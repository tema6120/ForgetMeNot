package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

sealed class RenameDeckEvent {
    class TextChanged(val text: String) : RenameDeckEvent()
    object OkButtonClicked : RenameDeckEvent()
}