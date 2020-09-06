package com.odnovolov.forgetmenot.presentation.screen.speakplan

sealed class SpeakPlanUiEvent {
    object HelpButtonClicked : SpeakPlanUiEvent()
    class SpeakEventButtonClicked(val position: Int) : SpeakPlanUiEvent()
    class RemoveSpeakEventButtonClicked(val position: Int) : SpeakPlanUiEvent()
    object AddSpeakEventButtonClicked : SpeakPlanUiEvent()
    object DialogOkButtonClicked : SpeakPlanUiEvent()
    object SpeakQuestionRadioButtonClicked : SpeakPlanUiEvent()
    object SpeakAnswerRadioButtonClicked : SpeakPlanUiEvent()
    object DelayButtonClicked : SpeakPlanUiEvent()
    class DelayInputChanged(val delayInput: String) : SpeakPlanUiEvent()
    class SpeakEventItemsMoved(val fromPosition: Int, val toPosition: Int) : SpeakPlanUiEvent()
}