package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.entity.Speaker
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.ANSWER
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.QUESTION
import java.util.*

class PronunciationController(
    private val pronunciationSettings: PronunciationSettings,
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationScreenState: PronunciationScreenState,
    private val speaker: Speaker,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<PronunciationEvent, Nothing>() {
    override fun handle(event: PronunciationEvent) {
        when (event) {
            TestPronunciationOfQuestionButtonClicked -> {
                pronunciationScreenState.whatIsPronounced = QUESTION
                val randomQuestion: String =
                    deckSettingsState.deck.cards.map { it.question }.random()
                val questionLanguage: Locale? =
                    deckSettingsState.deck.exercisePreference.pronunciation.questionLanguage
                speaker.speak(randomQuestion, questionLanguage)
            }

            StopSpeakButtonClicked -> {
                speaker.stop()
            }

            is QuestionLanguageSelected -> {
                pronunciationSettings.setQuestionLanguage(event.language)
            }

            QuestionAutoSpeakSwitchToggled -> {
                pronunciationSettings.toggleQuestionAutoSpeak()
            }

            TestPronunciationOfAnswerButtonClicked -> {
                pronunciationScreenState.whatIsPronounced = ANSWER
                val randomAnswer: String = deckSettingsState.deck.cards.map { it.answer }.random()
                val answerLanguage: Locale? =
                    deckSettingsState.deck.exercisePreference.pronunciation.answerLanguage
                speaker.speak(randomAnswer, answerLanguage)
            }

            is AnswerLanguageSelected -> {
                pronunciationSettings.setAnswerLanguage(event.language)
            }

            AnswerAutoSpeakSwitchToggled -> {
                pronunciationSettings.toggleAnswerAutoSpeak()
            }

            SpeakTextInBracketsSwitchToggled -> {
                pronunciationSettings.toggleSpeakTextInBrackets()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}