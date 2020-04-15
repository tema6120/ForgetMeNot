package com.odnovolov.forgetmenot.presentation.screen.speakplan

sealed class SpeakPlanSettingsEvent {
    class SpeakEventButtonClicked(val id: Long) : SpeakPlanSettingsEvent()
    class RemoveSpeakEventButtonClicked(val id: Long) : SpeakPlanSettingsEvent()
    object AddSpeakEventButtonClicked : SpeakPlanSettingsEvent()
    object DialogOkButtonClicked : SpeakPlanSettingsEvent()
    object SpeakQuestionRadioButtonClicked : SpeakPlanSettingsEvent()
    object SpeakAnswerRadioButtonClicked : SpeakPlanSettingsEvent()
    object DelayButtonClicked : SpeakPlanSettingsEvent()
    class DelayInputChanged(val delayInput: String) : SpeakPlanSettingsEvent()
}