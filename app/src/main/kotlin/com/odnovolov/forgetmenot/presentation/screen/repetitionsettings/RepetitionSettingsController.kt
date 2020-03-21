package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.StateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsController(
    private val repetitionSettings: RepetitionSettings,
    private val navigator: Navigator,
    private val repetitionSettingsStateProvider: StateProvider<RepetitionSettings.State>
) {
    fun onLevelOfKnowledgeRangeChanged(levelOfKnowledgeRange: IntRange) {
        repetitionSettings.setLevelOfKnowledgeRange(levelOfKnowledgeRange)
    }

    fun onStartRepetitionMenuItemClicked() {
        val repetitionState: Repetition.State = repetitionSettings.createRepetitionState()
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navigator.navigateToRepetition()
    }

    fun onFragmentPause() {
        repetitionSettingsStateProvider.save(repetitionSettings.state)
    }
}