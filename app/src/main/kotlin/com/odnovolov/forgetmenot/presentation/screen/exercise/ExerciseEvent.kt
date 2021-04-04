package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture

sealed class ExerciseEvent {
    class PageWasChanged(val position: Int) : ExerciseEvent()
    class GradeWasSelected(val grade: Int) : ExerciseEvent()
    object MarkAsLearnedButtonClicked : ExerciseEvent()
    object MarkAsUnlearnedButtonClicked : ExerciseEvent()
    object SpeakButtonClicked : ExerciseEvent()
    object StopSpeakButtonClicked : ExerciseEvent()
    object StopTimerButtonClicked : ExerciseEvent()
    object GetVariantsButtonClicked : ExerciseEvent()
    object MaskLettersButtonClicked : ExerciseEvent()
    object EditDeckSettingsButtonClicked : ExerciseEvent()
    object EditCardButtonClicked : ExerciseEvent()
    object SearchButtonClicked : ExerciseEvent()
    object WalkingModeSettingsButtonClicked : ExerciseEvent()
    object WalkingModeHelpButtonClicked : ExerciseEvent()
    object WalkingModeSwitchToggled : ExerciseEvent()
    object HelpButtonClicked : ExerciseEvent()
    object FragmentResumed : ExerciseEvent()
    object FragmentPaused : ExerciseEvent()
    class KeyGestureWasDetected(val keyGesture: KeyGesture) : ExerciseEvent()
    object BackButtonClicked : ExerciseEvent()
    object ShowUnansweredCardButtonClicked : ExerciseEvent()
    object UserConfirmedExit : ExerciseEvent()
}