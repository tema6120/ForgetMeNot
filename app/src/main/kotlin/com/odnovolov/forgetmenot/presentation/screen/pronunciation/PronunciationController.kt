package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import java.util.*

class PronunciationController(
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationSettings: PronunciationSettings,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onQuestionLanguageSelected(language: Locale?) {
        pronunciationSettings.setQuestionLanguage(language)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onQuestionAutoSpeakSwitchToggled() {
        val newQuestionAutoSpeak: Boolean =
            deckSettingsState.deck.exercisePreference.pronunciation.questionAutoSpeak.not()
        pronunciationSettings.setQuestionAutoSpeak(newQuestionAutoSpeak)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerLanguageSelected(language: Locale?) {
        pronunciationSettings.setAnswerLanguage(language)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerAutoSpeakSwitchToggled() {
        val newAnswerAutoSpeak: Boolean =
            deckSettingsState.deck.exercisePreference.pronunciation.answerAutoSpeak.not()
        pronunciationSettings.setAnswerAutoSpeak(newAnswerAutoSpeak)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onDoNotSpeakTextInBracketsSwitchToggled() {
        val newDoNotSpeakTextInBrackets: Boolean =
            deckSettingsState.deck.exercisePreference.pronunciation.doNotSpeakTextInBrackets.not()
        pronunciationSettings.setDoNotSpeakTextInBrackets(newDoNotSpeakTextInBrackets)
        longTermStateSaver.saveStateByRegistry()
    }
}