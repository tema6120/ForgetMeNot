package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodEvent.*

class TestingMethodController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<TestingMethodEvent, Nothing>() {
    override fun handle(event: TestingMethodEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromTestingMethod {
                    HelpDiScope(HelpArticle.TestMethods)
                }
            }

            WithoutTestingRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Off)
                exercise.notifyExercisePreferenceChanged()
            }

            SelfTestingRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Manual)
                exercise.notifyExercisePreferenceChanged()
            }

            TestingWithVariantsRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Quiz)
                exercise.notifyExercisePreferenceChanged()
            }

            SpellCheckRadioButtonClicked -> {
                deckSettings.setTestingMethod(TestingMethod.Entry)
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}