package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.*
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
                            it.targetLevelOfKnowledge == event.targetLevelOfKnowledge
                        }
                    val modifyIntervalDialogState = ModifyIntervalDialogState(
                        targetLevelOfKnowledge = event.targetLevelOfKnowledge,
                        displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
                    )
                    ModifyIntervalDiScope.create(modifyIntervalDialogState)
                }
            }

            AddIntervalButtonClicked -> {
                val lastIntervalValue: DateTimeSpan = deckSettingsState.deck.exercisePreference
                    .intervalScheme!!.intervals.last().value
                intervalsSettings.addInterval(lastIntervalValue)
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