package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

sealed class QAEditorEvent {
    class QuestionInputChanged(val text: String) : QAEditorEvent()
    class AnswerInputChanged(val text: String) : QAEditorEvent()
}