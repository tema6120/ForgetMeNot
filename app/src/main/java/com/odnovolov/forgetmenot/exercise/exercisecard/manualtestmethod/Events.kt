package com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod

sealed class ExerciseCardManualTestMethodEvent {
    object RememberButtonClicked : ExerciseCardManualTestMethodEvent()
    object NotRememberButtonClicked : ExerciseCardManualTestMethodEvent()
}