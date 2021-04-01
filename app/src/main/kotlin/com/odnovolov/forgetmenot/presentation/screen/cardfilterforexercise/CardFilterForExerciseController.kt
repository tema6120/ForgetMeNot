package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise.Companion.CARD_FILTER_NO_LIMIT
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreatorWithFiltering
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDiScope
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogState
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.CardFilterForExerciseController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.CardFilterForExerciseController.Command.ShowNoCardIsReadyForExercise
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.CardFilterForExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit.CardLimitDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit.CardLimitDialogState
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings.Companion.DEFAULT_CARD_NUMBER_LIMITATION
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.Companion
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogCaller
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.days

class CardFilterForExerciseController(
    private val exerciseStateCreator: ExerciseStateCreatorWithFiltering,
    private val cardFilter: CardFilterForExercise,
    private val navigator: Navigator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseCreatorStateProvider: ShortTermStateProvider<ExerciseStateCreatorWithFiltering.State>
) : BaseController<CardFilterForExerciseEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForExercise : Command()
    }

    override fun handle(event: CardFilterForExerciseEvent) {
        when (event) {
            LimitButtonClicked -> {
                navigator.showCardLimitDialog {
                    val isNoLimit: Boolean =
                        globalState.cardFilterForExercise.limit == CARD_FILTER_NO_LIMIT
                    val dialogText: String =
                        if (isNoLimit) DEFAULT_CARD_NUMBER_LIMITATION.toString()
                        else globalState.cardFilterForExercise.limit.toString()
                    val dialogState = CardLimitDialogState(isNoLimit, dialogText)
                    CardLimitDiScope.create(dialogState)
                }
            }

            is GradeRangeChanged -> {
                cardFilter.gradeRange = event.gradeRange
            }

            LastTestedFromButtonClicked -> {
                showLastTestedFilterDialog(isFromDialog = true)
            }

            LastTestedToButtonClicked -> {
                showLastTestedFilterDialog(isFromDialog = false)
            }

            StartExerciseButtonClicked -> {
                if (exerciseStateCreator.hasAnyCardAvailableForExercise()) {
                    navigator.navigateToExerciseFromCardFilter {
                        val exerciseState: Exercise.State = exerciseStateCreator.create()
                        ExerciseDiScope.create(exerciseState)
                    }
                } else {
                    sendCommand(ShowNoCardIsReadyForExercise)
                }
            }
        }
    }

    private fun showLastTestedFilterDialog(isFromDialog: Boolean) {
        navigator.showLastTestedFilterDialogFromCardFilterForExercise {
            val dateTimeSpan: DateTimeSpan? =
                if (isFromDialog) cardFilter.lastTestedFromTimeAgo
                else cardFilter.lastTestedToTimeAgo
            val dialogState = LastTestedFilterDialogState(
                isFromDialog = isFromDialog,
                isZeroTimeSelected = dateTimeSpan == null,
                timeAgo = dateTimeSpan?.let(Companion::fromDateTimeSpan)
                    ?: DisplayedInterval.fromDateTimeSpan(7.days.toDateTimeSpan()),
                caller = LastTestedFilterDialogCaller.CardFilterForExercise
            )
            LastTestedFilterDiScope.create(dialogState)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        exerciseCreatorStateProvider.save(exerciseStateCreator.state)
    }
}