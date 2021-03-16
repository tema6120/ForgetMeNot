package com.odnovolov.forgetmenot.presentation.screen.cardappearance

sealed class CardAppearanceEvent{
    object AlignQuestionToEdgeButtonClicked : CardAppearanceEvent()
    object AlignQuestionToCenterButtonClicked : CardAppearanceEvent()
    class QuestionTextSizeTextChanged(val text: String) : CardAppearanceEvent()
    object AlignAnswerToEdgeButtonClicked : CardAppearanceEvent()
    object AlignAnswerToCenterButtonClicked : CardAppearanceEvent()
    class AnswerTextSizeTextChanged(val text: String) : CardAppearanceEvent()
}