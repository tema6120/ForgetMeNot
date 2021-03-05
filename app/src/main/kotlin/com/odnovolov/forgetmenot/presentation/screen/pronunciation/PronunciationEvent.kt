package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object HelpButtonClicked : PronunciationEvent()
    object CloseTipButtonClicked : PronunciationEvent()
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    object QuestionAutoSpeakSwitchToggled : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    object AnswerAutoSpeakSwitchToggled : PronunciationEvent()
    object SpeakTextInBracketsSwitchToggled : PronunciationEvent()
    class MarkedLanguageAsFavorite(val language: Locale) : PronunciationEvent()
    class UnmarkedLanguageAsFavorite(val language: Locale) : PronunciationEvent()
}