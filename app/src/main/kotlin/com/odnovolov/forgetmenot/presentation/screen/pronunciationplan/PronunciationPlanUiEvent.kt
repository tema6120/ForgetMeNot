package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

sealed class PronunciationPlanUiEvent {
    object HelpButtonClicked : PronunciationPlanUiEvent()
    class PronunciationEventButtonClicked(val position: Int) : PronunciationPlanUiEvent()
    class RemovePronunciationEventButtonClicked(val position: Int) : PronunciationPlanUiEvent()
    object AddPronunciationEventButtonClicked : PronunciationPlanUiEvent()
    object DialogOkButtonClicked : PronunciationPlanUiEvent()
    object SpeakQuestionRadioButtonClicked : PronunciationPlanUiEvent()
    object SpeakAnswerRadioButtonClicked : PronunciationPlanUiEvent()
    object DelayButtonClicked : PronunciationPlanUiEvent()
    class DelayInputChanged(val delayInput: String) : PronunciationPlanUiEvent()
    class PronunciationEventItemsMoved(val fromPosition: Int, val toPosition: Int) : PronunciationPlanUiEvent()
}