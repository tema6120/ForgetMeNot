package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToAddNewInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToChangeInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.soywiz.klock.DateTimeSpan

class IntervalsController(
    private val deckSettingsState: DeckSettings.State,
    private val intervalsSettings: IntervalsSettings,
    private val screenState: IntervalsScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<IntervalsScreenState>
) : BaseController<IntervalsEvent, Nothing>() {
    private val currentIntervalScheme: IntervalScheme?
        get() = deckSettingsState.deck.exercisePreference.intervalScheme

    override fun handle(event: IntervalsEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromIntervals {
                    HelpArticleDiScope(HelpArticle.GradeAndIntervals)
                }
            }

            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
            }

            IntervalsSwitchToggled -> {
                if (currentIntervalScheme == null) {
                    intervalsSettings.turnOnIntervals()
                } else {
                    intervalsSettings.turnOffIntervals()
                }
            }

            is IntervalButtonClicked -> {
                val interval: Interval = currentIntervalScheme?.intervals?.first {
                    it.grade == event.grade
                } ?: return
                navigator.showModifyIntervalDialog {
                    val modifyIntervalDialogState = ModifyIntervalDialogState(
                        dialogPurpose = ToChangeInterval,
                        grade = event.grade,
                        displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
                    )
                    ModifyIntervalDiScope.create(modifyIntervalDialogState)
                }
            }

            AddIntervalButtonClicked -> {
                val lastInterval = currentIntervalScheme?.intervals?.last() ?: return
                val grade: Int = lastInterval.grade + 1
                val value: DateTimeSpan = lastInterval.value
                val displayedInterval = DisplayedInterval.fromDateTimeSpan(value)
                navigator.showModifyIntervalDialog {
                    val modifyIntervalDialogState = ModifyIntervalDialogState(
                        dialogPurpose = ToAddNewInterval,
                        grade,
                        displayedInterval
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
        screenStateProvider.save(screenState)
    }
}