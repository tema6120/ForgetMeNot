package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.entity.KeyGesture

sealed class ExerciseEvent {
    class NewPageBecameSelected(val position: Int) : ExerciseEvent()
    object NotAskButtonClicked : ExerciseEvent()
    object UndoButtonClicked : ExerciseEvent()
    object SpeakButtonClicked : ExerciseEvent()
    object EditCardButtonClicked : ExerciseEvent()
    object HintButtonClicked : ExerciseEvent()
    object HintAsQuizButtonClicked : ExerciseEvent()
    object HintMaskLettersButtonClicked : ExerciseEvent()
    object AnswerAutoSpeakTriggered : ExerciseEvent()
    object LevelOfKnowledgeButtonClicked : ExerciseEvent()
    class LevelOfKnowledgeSelected(val levelOfKnowledge: Int) : ExerciseEvent()
    class KeyGestureDetected(val keyGesture: KeyGesture) : ExerciseEvent()
}