package com.odnovolov.forgetmenot.pronunciation

sealed class PronunciationOrder {
    class SetDialogText(val text: String) : PronunciationOrder()
}