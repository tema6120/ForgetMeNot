package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

sealed class AnswerQuizTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerQuizTestEvent()
    class VariantSelected(val variantNumber: Int) : AnswerQuizTestEvent()
}