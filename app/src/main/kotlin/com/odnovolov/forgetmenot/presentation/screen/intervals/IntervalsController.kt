package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.MODIFY_INTERVAL_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalViewModel
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.getKoin

class IntervalsController(
    private val deckSettingsState: DeckSettings.State,
    private val intervalsSettings: IntervalsSettings,
    private val longTermStateSaver: LongTermStateSaver
) {
    private val commandFlow = EventFlow<IntervalsCommand>()
    val commands: Flow<IntervalsCommand> = commandFlow.get()

    fun onModifyIntervalButtonClicked(targetLevelOfKnowledge: Int) {
        val interval: Interval = deckSettingsState.deck.exercisePreference.intervalScheme
            ?.intervals?.find { it.targetLevelOfKnowledge == targetLevelOfKnowledge } ?: return
        val modifyIntervalDialogState = ModifyIntervalDialogState(
            targetLevelOfKnowledge = targetLevelOfKnowledge,
            displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
        )
        val koinScope = getKoin().createScope<ModifyIntervalViewModel>(MODIFY_INTERVAL_SCOPE_ID)
        koinScope.declare(modifyIntervalDialogState, override = true)
        commandFlow.send(ShowModifyIntervalDialog)
    }

    fun onAddIntervalButtonClicked() {
        val lastIntervalValue: DateTimeSpan = deckSettingsState.deck.exercisePreference
            .intervalScheme?.intervals?.last()?.value ?: return
        intervalsSettings.addInterval(lastIntervalValue)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onRemoveIntervalButtonClicked() {
        intervalsSettings.removeLastInterval()
        longTermStateSaver.saveStateByRegistry()
    }
}