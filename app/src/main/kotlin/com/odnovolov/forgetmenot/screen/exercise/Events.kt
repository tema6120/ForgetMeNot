package com.odnovolov.forgetmenot.screen.exercise

sealed class ExerciseEvent {
    class NewPageBecameSelected(val position: Int) : ExerciseEvent()
    object NotAskButtonClicked : ExerciseEvent()
    object UndoButtonClicked : ExerciseEvent()
    object SpeakButtonClicked : ExerciseEvent()
    object EditCardButtonClicked : ExerciseEvent()
    object AnswerAutoSpeakTriggered : ExerciseEvent()
    object LevelOfKnowledgeButtonClicked : ExerciseEvent()
    class LevelOfKnowledgeSelected(val levelOfKnowledge: Int) : ExerciseEvent()
}