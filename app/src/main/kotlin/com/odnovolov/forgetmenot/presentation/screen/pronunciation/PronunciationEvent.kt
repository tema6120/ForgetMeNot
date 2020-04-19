package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object TestPronunciationOfQuestionButtonClicked : PronunciationEvent()
    object StopSpeakButtonClicked : PronunciationEvent()
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    object QuestionAutoSpeakSwitchToggled : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    object TestPronunciationOfAnswerButtonClicked : PronunciationEvent()
    object AnswerAutoSpeakSwitchToggled : PronunciationEvent()
    object SpeakTextInBracketsSwitchToggled : PronunciationEvent()
}