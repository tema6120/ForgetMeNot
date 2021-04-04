package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object HelpButtonClicked : PronunciationEvent()
    object CloseTipButtonClicked : PronunciationEvent()
    class QuestionLanguageWasSelected(val language: Locale?) : PronunciationEvent()
    object QuestionAutoSpeakSwitchToggled : PronunciationEvent()
    class AnswerLanguageWasSelected(val language: Locale?) : PronunciationEvent()
    object AnswerAutoSpeakSwitchToggled : PronunciationEvent()
    object SpeakTextInBracketsSwitchToggled : PronunciationEvent()
    class LanguageWasMarkedAsFavorite(val language: Locale) : PronunciationEvent()
    class LanguageWasUnmarkedAsFavorite(val language: Locale) : PronunciationEvent()
}