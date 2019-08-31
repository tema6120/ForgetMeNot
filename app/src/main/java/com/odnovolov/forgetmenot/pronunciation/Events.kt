package com.odnovolov.forgetmenot.pronunciation

import java.util.*

sealed class PronunciationEvent {
    class AvailableLanguagesUpdated(val languages: Set<Locale>) : PronunciationEvent()
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    object QuestionAutoSpeakSwitchClicked : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    object AnswerAutoSpeakSwitchClicked : PronunciationEvent()
}