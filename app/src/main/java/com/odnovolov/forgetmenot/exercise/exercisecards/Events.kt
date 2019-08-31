package com.odnovolov.forgetmenot.exercise.exercisecards

sealed class ExerciseCardEvent {
    object ShowAnswerButtonClicked : ExerciseCardEvent()
}

