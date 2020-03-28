package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import LAST_ANSWER_FILTER_SCOPE_ID
import REPETITION_LAPS_SCOPE_ID
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings.NoCardIsReadyForRepetition
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsViewModel
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterViewModel
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.days
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsController(
    private val repetitionSettings: RepetitionSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionSettingsStateProvider: UserSessionTermStateProvider<RepetitionSettings.State>
) {
    sealed class Command {
        object ShowNoCardIsReadyForRepetitionMessage : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun onAvailableForExerciseGroupButtonClicked() {
        with(repetitionSettings) {
            setIsAvailableForExerciseCardsIncluded(!state.isAvailableForExerciseCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAwaitingGroupButtonClicked() {
        with(repetitionSettings) {
            setIsAwaitingCardsIncluded(!state.isAwaitingCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLearnedGroupButtonClicked() {
        with(repetitionSettings) {
            setIsLearnedCardsIncluded(!state.isLearnedCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLevelOfKnowledgeRangeChanged(levelOfKnowledgeRange: IntRange) {
        repetitionSettings.setLevelOfKnowledgeRange(levelOfKnowledgeRange)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onStartRepetitionMenuItemClicked() {
        val repetitionState: Repetition.State = try {
            repetitionSettings.createRepetitionState()
        } catch (e: NoCardIsReadyForRepetition) {
            commandFlow.send(ShowNoCardIsReadyForRepetitionMessage)
            return
        }
        longTermStateSaver.saveStateByRegistry()
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navigator.navigateToRepetition()
    }

    fun onLastAnswerFromButtonClicked() {
        showLastAnswerFilterDialog(isFromDialog = true)
    }

    fun onLastAnswerToButtonClicked() {
        showLastAnswerFilterDialog(isFromDialog = false)
    }

    private fun showLastAnswerFilterDialog(isFromDialog: Boolean) {
        val dateTimeSpan: DateTimeSpan? =
            if (isFromDialog) repetitionSettings.state.lastAnswerFromTimeAgo
            else repetitionSettings.state.lastAnswerToTimeAgo
        val dialogState = LastAnswerFilterDialogState(
            isFromDialog = isFromDialog,
            isZeroTimeSelected = dateTimeSpan == null,
            timeAgo = dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
                ?: DisplayedInterval.fromDateTimeSpan(7.days.toDateTimeSpan())
        )
        val koinScope =
            getKoin().createScope<LastAnswerFilterViewModel>(LAST_ANSWER_FILTER_SCOPE_ID)
        koinScope.declare(dialogState, override = true)
        navigator.showLastAnswerFilterDialog()
    }

    fun onLapsButtonClicked() {
        val isInfinite = repetitionSettings.state.numberOfLaps == Int.MAX_VALUE
        val numberOfLapsInput: String =
            if (isInfinite) "1"
            else repetitionSettings.state.numberOfLaps.toString()
        val dialogState = RepetitionLapsDialogState(
            isInfinitely = isInfinite,
            numberOfLapsInput = numberOfLapsInput
        )
        val koinScope = getKoin().createScope<RepetitionLapsViewModel>(REPETITION_LAPS_SCOPE_ID)
        koinScope.declare(dialogState, override = true)
        navigator.showRepetitionLapsDialog()
    }

    fun onFragmentPause() {
        repetitionSettingsStateProvider.save(repetitionSettings.state)
    }
}