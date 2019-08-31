package com.odnovolov.forgetmenot.pronunciation

sealed class PronunciationOrder {
    object ShowQuestionDropdownList : PronunciationOrder()
    object DismissQuestionDropdownList : PronunciationOrder()
    object ShowAnswerDropdownList : PronunciationOrder()
    object DismissAnswerDropdownList : PronunciationOrder()
    data class SetNameErrorText(val errorText: String) : PronunciationOrder()
    object NavigateUp : PronunciationOrder()
}