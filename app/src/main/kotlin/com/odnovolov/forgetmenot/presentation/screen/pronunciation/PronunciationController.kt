package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*

class PronunciationController(
    private val pronunciationSettings: PronunciationSettings,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<PronunciationEvent, Nothing>() {
    override fun handle(event: PronunciationEvent) {
        when (event) {
            is QuestionLanguageSelected -> {
                pronunciationSettings.setQuestionLanguage(event.language)
            }

            QuestionAutoSpeakSwitchToggled -> {
                pronunciationSettings.toggleQuestionAutoSpeak()
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