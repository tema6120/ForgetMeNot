package com.odnovolov.forgetmenot.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object SavePronunciationButtonClicked : PronunciationEvent()
    class AvailableLanguagesUpdated(val languages: Set<Locale>) : PronunciationEvent()
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    class QuestionAutoSpeakSwitchToggled(val isOn: Boolean) : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    class AnswerAutoSpeakSwitchToggled(val isOn: Boolean) : PronunciationEvent()
}