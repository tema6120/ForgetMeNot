package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip.*
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodEvent.*

class TestingMethodController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val screenState: TestingMethodScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<TestingMethodScreenState>
) : BaseController<TestingMethodEvent, Nothing>() {
    override fun handle(event: TestingMethodEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromTestingMethod {
                    HelpArticleDiScope(HelpArticle.TestingMethods)
                }
            }

            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
            }

            WithoutTestingRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Off)
                switchTip(TipTestingMethodScreenWithoutTesting)
                exercise.notifyExercisePreferenceChanged()
            }

            SelfTestingRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Manual)
                switchTip(TipTestingMethodScreenSelfTesting)
                exercise.notifyExercisePreferenceChanged()
            }

            TestingWithVariantsRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Quiz)
                switchTip(TipTestingMethodScreenTestingWithVariants)
                exercise.notifyExercisePreferenceChanged()
            }

            SpellCheckRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Entry)
                switchTip(TipTestingMethodScreenSpellCheck)
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    private fun switchTip(tip: Tip) {
        screenState.tip = if (tip.state.needToShow) tip else null
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}