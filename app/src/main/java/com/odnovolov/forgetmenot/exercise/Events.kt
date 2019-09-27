package com.odnovolov.forgetmenot.exercise

sealed class ExerciseEvent {
    class NewPageBecameSelected(val position: Int) : ExerciseEvent()
    object NotAskButtonClicked : ExerciseEvent()
    object UndoButtonClicked : ExerciseEvent()
    object SpeakButtonClicked : ExerciseEvent()
    object EditCardButtonClicked : ExerciseEvent()
}