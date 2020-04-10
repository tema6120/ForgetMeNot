package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

sealed class QuizTestExerciseCardEvent {
    object ShowQuestionButtonClicked : QuizTestExerciseCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : QuizTestExerciseCardEvent()
    class VariantSelected(val variantIndex: Int) : QuizTestExerciseCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : QuizTestExerciseCardEvent()
}