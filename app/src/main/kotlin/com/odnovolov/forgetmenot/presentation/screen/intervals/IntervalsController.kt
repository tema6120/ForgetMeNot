package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToAddNewInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToChangeInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.soywiz.klock.DateTimeSpan

class IntervalsController(
    private val deckSettingsState: DeckSettings.State,
    private val intervalsSettings: IntervalsSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<IntervalsEvent, Nothing>() {
    override fun handle(event: IntervalsEvent) {
        when (event) {
            is ModifyIntervalButtonClicked -> {
                navigator.showModifyIntervalDialog {
                    val interval: Interval = deckSettingsState.deck.exercisePreference
                        .intervalScheme!!.intervals.first {
                            it.levelOfKnowledge == event.levelOfKnowledge
                        }
                    val modifyIntervalDialogState = ModifyIntervalDialogState(
                        dialogPurpose = ToChangeInterval(event.levelOfKnowledge),
                        displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
                    )
                    ModifyIntervalDiScope.create(modifyIntervalDialogState)
                }
            }

            AddIntervalButtonClicked -> {
                navigator.showModifyIntervalDialog {
                    val lastIntervalValue: DateTimeSpan = deckSettingsState.deck.exercisePreference
                        .intervalScheme!!.intervals.last().value
                    val modifyIntervalDialogState = ModifyIntervalDialogState(
                        dialogPurpose = ToAddNewInterval,
                        displayedInterval = DisplayedInterval.fromDateTimeSpan(lastIntervalValue)
                    )
                    ModifyIntervalDiScope.create(modifyIntervalDialogState)
                }
            }

            RemoveIntervalButtonClicked -> {
                intervalsSettings.removeLastInterval()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}