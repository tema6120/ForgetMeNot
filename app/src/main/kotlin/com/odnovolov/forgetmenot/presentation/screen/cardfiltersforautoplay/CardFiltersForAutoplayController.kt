package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay

import com.odnovolov.forgetmenot.domain.entity.CardFiltersForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.CardFiltersForAutoplayController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.CardFiltersForAutoplayController.Command.ShowNoCardIsReadyForRepetitionMessage
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.CardFiltersForAutoplayEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested.LastTestedFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested.LastTestedFilterDialogState
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.days

class CardFiltersForAutoplayController(
    private val repetitionStateCreator: RepetitionStateCreator,
    private val cardFilters: CardFiltersForAutoplay,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionCreatorStateProvider: ShortTermStateProvider<RepetitionStateCreator.State>
) : BaseController<CardFiltersForAutoplayEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForRepetitionMessage : Command()
    }

    override fun handle(event: CardFiltersForAutoplayEvent) {
        when (event) {
            AvailableForExerciseCheckboxClicked -> {
                cardFilters.isAvailableForExerciseCardsIncluded =
                    !cardFilters.isAvailableForExerciseCardsIncluded
            }

            AwaitingCheckboxClicked -> {
                cardFilters.isAwaitingCardsIncluded = !cardFilters.isAwaitingCardsIncluded
            }

            LearnedCheckboxClicked -> {
                cardFilters.isLearnedCardsIncluded = !cardFilters.isLearnedCardsIncluded
            }

            is GradeRangeChanged -> {
                cardFilters.gradeRange = event.gradeRange
            }

            LastTestedFromButtonClicked -> {
                showLastTestedFilterDialog(isFromDialog = true)
            }

            LastTestedToButtonClicked -> {
                showLastTestedFilterDialog(isFromDialog = false)
            }

            StartPlayingButtonClicked -> {
                if (repetitionStateCreator.hasAnyCardAvailableForRepetition()) {
                    navigator.navigateToRepetition {
                        val repetitionState: Repetition.State = repetitionStateCreator.create()
                        RepetitionDiScope.create(repetitionState)
                    }
                } else {
                    sendCommand(ShowNoCardIsReadyForRepetitionMessage)
                }
            }
        }
    }

    private fun showLastTestedFilterDialog(isFromDialog: Boolean) {
        navigator.showLastTestedFilterDialog {
            val dateTimeSpan: DateTimeSpan? =
                if (isFromDialog) cardFilters.lastTestedFromTimeAgo
                else cardFilters.lastTestedToTimeAgo
            val dialogState = LastTestedFilterDialogState(
                isFromDialog = isFromDialog,
                isZeroTimeSelected = dateTimeSpan == null,
                timeAgo = dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
                    ?: DisplayedInterval.fromDateTimeSpan(7.days.toDateTimeSpan())
            )
            LastTestedFilterDiScope.create(dialogState)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        repetitionCreatorStateProvider.save(repetitionStateCreator.state)
    }
}