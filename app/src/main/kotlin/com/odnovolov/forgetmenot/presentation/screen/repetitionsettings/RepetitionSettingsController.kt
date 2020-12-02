package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDialogState
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.days

class RepetitionSettingsController(
    private val repetitionSettings: RepetitionSettings,
    private val repetitionStateCreator: RepetitionStateCreator,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionCreatorStateProvider: ShortTermStateProvider<RepetitionStateCreator.State>
) : BaseController<RepetitionSettingsEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForRepetitionMessage : Command()
    }

    override fun handle(event: RepetitionSettingsEvent) {
        when (event) {
            StartRepetitionMenuItemClicked -> {
                if (repetitionStateCreator.hasAnyCardAvailableForRepetition()) {
                    navigator.navigateToRepetition {
                        val repetitionState: Repetition.State = repetitionStateCreator.create()
                        RepetitionDiScope.create(repetitionState)
                    }
                } else {
                    sendCommand(ShowNoCardIsReadyForRepetitionMessage)
                }
            }

            AvailableForExerciseGroupButtonClicked -> {
                repetitionSettings.toggleIsAvailableForExerciseCardsIncluded()
            }

            AwaitingGroupButtonClicked -> {
                repetitionSettings.toggleIsAwaitingCardsIncluded()
            }

            LearnedGroupButtonClicked -> {
                repetitionSettings.toggleIsLearnedCardsIncluded()
            }

            is GradeRangeChanged -> {
                repetitionSettings.setLevelOfKnowledgeRange(event.levelOfKnowledgeRange)
            }

            LastAnswerFromButtonClicked -> {
                showLastAnswerFilterDialog(isFromDialog = true)
            }

            LastAnswerToButtonClicked -> {
                showLastAnswerFilterDialog(isFromDialog = false)
            }

            LapsButtonClicked -> {
                navigator.showRepetitionLapsDialog {
                    val isInfinite =
                        globalState.currentRepetitionSetting.numberOfLaps == Int.MAX_VALUE
                    val numberOfLapsInput: String =
                        if (isInfinite) "1"
                        else globalState.currentRepetitionSetting.numberOfLaps.toString()
                    val dialogState = RepetitionLapsDialogState(isInfinite, numberOfLapsInput)
                    RepetitionLapsDiScope.create(dialogState)
                }
            }
        }
    }

    private fun showLastAnswerFilterDialog(isFromDialog: Boolean) {
        navigator.showLastAnswerFilterDialog {
            val dateTimeSpan: DateTimeSpan? =
                if (isFromDialog) globalState.currentRepetitionSetting.lastAnswerFromTimeAgo
                else globalState.currentRepetitionSetting.lastAnswerToTimeAgo
            val dialogState = LastAnswerFilterDialogState(
                isFromDialog = isFromDialog,
                isZeroTimeSelected = dateTimeSpan == null,
                timeAgo = dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
                    ?: DisplayedInterval.fromDateTimeSpan(7.days.toDateTimeSpan())
            )
            LastAnswerFilterDiScope.create(dialogState)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        repetitionCreatorStateProvider.save(repetitionStateCreator.state)
    }
}