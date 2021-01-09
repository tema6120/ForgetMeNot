package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*

class PronunciationController(
    private val pronunciationSettings: PronunciationSettings,
    private val screenState: PronunciationScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<PronunciationScreenState>
) : BaseController<PronunciationEvent, Nothing>() {
    override fun handle(event: PronunciationEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromPronunciation {
                    HelpDiScope(HelpArticle.Pronunciation)
                }
            }

            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
            }

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
        screenStateProvider.save(screenState)
    }
}