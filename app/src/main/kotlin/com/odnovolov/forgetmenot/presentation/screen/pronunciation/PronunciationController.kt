package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import java.util.*

class PronunciationController(
    private val pronunciationSettings: PronunciationSettings,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onQuestionLanguageSelected(language: Locale?) {
        pronunciationSettings.setQuestionLanguage(language)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onQuestionAutoSpeakSwitchToggled() {
        pronunciationSettings.toggleQuestionAutoSpeak()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerLanguageSelected(language: Locale?) {
        pronunciationSettings.setAnswerLanguage(language)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerAutoSpeakSwitchToggled() {
        pronunciationSettings.toggleAnswerAutoSpeak()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSpeakTextInBracketsSwitchToggled() {
        pronunciationSettings.toggleSpeakTextInBrackets()
        longTermStateSaver.saveStateByRegistry()
    }
}