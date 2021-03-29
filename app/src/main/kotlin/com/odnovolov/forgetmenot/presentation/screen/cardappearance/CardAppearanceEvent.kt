package com.odnovolov.forgetmenot.presentation.screen.cardappearance

sealed class CardAppearanceEvent{
    object AlignQuestionToEdgeButtonClicked : CardAppearanceEvent()
    object AlignQuestionToCenterButtonClicked : CardAppearanceEvent()
    object QuestionTextSizeButtonClicked : CardAppearanceEvent()
    object AlignAnswerToEdgeButtonClicked : CardAppearanceEvent()
    object AlignAnswerToCenterButtonClicked : CardAppearanceEvent()
    object AnswerTextSizeButtonClicked : CardAppearanceEvent()
}