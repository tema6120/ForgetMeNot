package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

sealed class ExerciseCardEvent {
    object ShowAnswerButtonClicked : ExerciseCardEvent()
}