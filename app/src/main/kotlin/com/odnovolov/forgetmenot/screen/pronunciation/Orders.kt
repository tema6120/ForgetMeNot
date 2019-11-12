package com.odnovolov.forgetmenot.screen.pronunciation

sealed class PronunciationOrder {
    class SetDialogText(val text: String) : PronunciationOrder()
}