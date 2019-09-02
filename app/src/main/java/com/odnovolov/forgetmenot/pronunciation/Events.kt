package com.odnovolov.forgetmenot.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object SavePronunciationButtonClicked : PronunciationEvent()
    object AddNewPronunciationButtonClicked : PronunciationEvent()
    class DialogTextChanged(val text: String) : PronunciationEvent()
    object PositiveDialogButtonClicked : PronunciationEvent()
    object NegativeDialogButtonClicked : PronunciationEvent()
    class AvailableLanguagesUpdated(val languages: Set<Locale>) : PronunciationEvent()
    class QuestionLanguageSelected(val language: Locale?) : PronunciationEvent()
    class QuestionAutoSpeakSwitchToggled(val isOn: Boolean) : PronunciationEvent()
    class AnswerLanguageSelected(val language: Locale?) : PronunciationEvent()
    class AnswerAutoSpeakSwitchToggled(val isOn: Boolean) : PronunciationEvent()
}