package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import java.util.*

sealed class PronunciationEvent {
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    object QuestionAutoSpeakSwitchToggled : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    object AnswerAutoSpeakSwitchToggled : PronunciationEvent()
    object SpeakTextInBracketsSwitchToggled : PronunciationEvent()
}