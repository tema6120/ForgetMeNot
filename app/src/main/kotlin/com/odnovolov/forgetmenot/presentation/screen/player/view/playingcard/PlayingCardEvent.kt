package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

sealed class PlayingCardEvent {
    object ShowQuestionButtonClicked : PlayingCardEvent()
    object ShowAnswerButtonClicked : PlayingCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : PlayingCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : PlayingCardEvent()
}