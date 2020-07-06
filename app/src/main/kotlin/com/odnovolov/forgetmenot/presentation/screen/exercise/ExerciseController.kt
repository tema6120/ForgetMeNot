package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

class ExerciseController(
    private val exercise: Exercise,
    private val walkingModePreference: WalkingModePreference,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseStateProvider: ShortTermStateProvider<Exercise.State>
) : BaseController<ExerciseEvent, Command>() {
    sealed class Command {
        object MoveToNextPosition : Command()
        object MoveToPreviousPosition : Command()
        object ShowChooseHintPopup : Command()
        class ShowLevelOfKnowledgePopup(val intervalItems: List<IntervalItem>) : Command()
        object ShowIntervalsAreOffMessage : Command()
    }

    override fun handle(event: ExerciseEvent) {
        when (event) {
            is PageSelected -> {
                exercise.setCurrentPosition(event.position)
            }

            NotAskButtonClicked -> {
                exercise.setIsCardLearned(true)
                sendCommand(MoveToNextPosition)
            }

            AskAgainButtonClicked -> {
                exercise.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                exercise.speak()
            }

            StopSpeakButtonClicked -> {
                exercise.stopSpeaking()
            }

            EditCardButtonClicked -> {
                navigator.navigateToCardEditorFromExercise {
                    val editableCard = EditableCard(exercise.currentExerciseCard.base.card)
                    OngoingCardEditorDiScope.create(editableCard)
                }
            }

            HintButtonClicked -> {
                if (exercise.currentExerciseCard.base.hint == null) {
                    sendCommand(ShowChooseHintPopup)
                } else {
                    exercise.showHint()
                }
            }

            HintAsQuizButtonClicked -> {
                exercise.hintAsQuiz()
            }

            MaskLettersButtonClicked -> {
                exercise.showHint()
            }

            TimerButtonClicked -> {
                exercise.stopTimer()
            }

            FragmentResumed -> {
                exercise.startTimer()
            }

            FragmentPaused -> {
                exercise.resetTimer()
            }

            LevelOfKnowledgeButtonClicked -> {
                onLevelOfKnowledgeButtonClicked()
            }

            is LevelOfKnowledgeSelected -> {
                exercise.setLevelOfKnowledge(event.levelOfKnowledge)
            }

            is KeyGestureDetected -> {
                onKeyGestureDetected(event)
            }
        }
    }

    private fun onLevelOfKnowledgeButtonClicked() {
        val intervalScheme: IntervalScheme? =
            exercise.currentExerciseCard.base.deck.exercisePreference.intervalScheme
        if (intervalScheme == null) {
            sendCommand(ShowIntervalsAreOffMessage)
        } else {
            val currentLevelOfKnowledge: Int =
                exercise.currentExerciseCard.base.card.levelOfKnowledge
            val intervalItems: List<IntervalItem> = intervalScheme.intervals
                .map { interval: Interval ->
                    IntervalItem(
                        levelOfKnowledge = interval.levelOfKnowledge,
                        waitingPeriod = interval.value,
                        isSelected = currentLevelOfKnowledge == interval.levelOfKnowledge
                    )
                }
            sendCommand(ShowLevelOfKnowledgePopup(intervalItems))
        }
    }

    private fun onKeyGestureDetected(event: KeyGestureDetected) {
        val keyGestureAction: KeyGestureAction =
            walkingModePreference.keyGestureMap[event.keyGesture] ?: return
        when (keyGestureAction) {
            NO_ACTION -> return
            MOVE_TO_NEXT_CARD -> sendCommand(MoveToNextPosition)
            MOVE_TO_PREVIOUS_CARD -> sendCommand(MoveToPreviousPosition)
            SET_CARD_AS_REMEMBER -> exercise.answer(Remember)
            SET_CARD_AS_NOT_REMEMBER -> exercise.answer(NotRemember)
            SET_CARD_AS_LEARNED -> {
                exercise.setIsCardLearned(true)
                sendCommand(MoveToNextPosition)
            }
            SPEAK_QUESTION -> exercise.speakQuestion()
            SPEAK_ANSWER -> exercise.speakAnswer()
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        exerciseStateProvider.save(exercise.state)
    }
}