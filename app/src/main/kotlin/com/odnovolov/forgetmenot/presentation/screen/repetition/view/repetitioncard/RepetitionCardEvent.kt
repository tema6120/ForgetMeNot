package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

sealed class RepetitionCardEvent {
    object ShowQuestionButtonClicked : RepetitionCardEvent()
    object ShowAnswerButtonClicked : RepetitionCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : RepetitionCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : RepetitionCardEvent()
}