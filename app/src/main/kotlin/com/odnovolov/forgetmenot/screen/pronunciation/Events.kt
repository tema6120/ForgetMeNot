package com.odnovolov.forgetmenot.screen.pronunciation

import java.util.*

sealed class PronunciationEvent {
    object SavePronunciationButtonClicked : PronunciationEvent()
    class SetPronunciationButtonClicked(val pronunciationId: Long) : PronunciationEvent()
    class RenamePronunciationButtonClicked(val pronunciationId: Long) : PronunciationEvent()
    class DeletePronunciationButtonClicked(val pronunciationId: Long) : PronunciationEvent()
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