package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.*

class QuestionDisplayController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val screenState: QuestionDisplayScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<QuestionDisplayScreenState>
) : BaseController<QuestionDisplayEvent, Nothing>() {
    override fun handle(event: QuestionDisplayEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromQuestionDisplay {
                    val screenState = HelpArticleScreenState(HelpArticle.QuestionDisplay)
                    HelpArticleDiScope.create(screenState)
                }
            }

            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
            }

            QuestionDisplaySwitchToggled -> {
                deckSettings.toggleIsQuestionDisplayed()
                with(deckSettings.state.deck.exercisePreference) {
                    if (!isQuestionDisplayed && !pronunciation.questionAutoSpeaking) {
                        screenState.tip = Tip.TipQuestionDisplayScreenDoNotForgetAutospeaking
                    }
                    if (isQuestionDisplayed
                        && screenState.tip == Tip.TipQuestionDisplayScreenDoNotForgetAutospeaking
                    ) {
                        screenState.tip = null
                    }
                }
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}