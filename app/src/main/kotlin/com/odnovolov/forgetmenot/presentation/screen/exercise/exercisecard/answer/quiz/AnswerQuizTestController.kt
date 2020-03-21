package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Variant
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestCommand.Vibrate
import kotlinx.coroutines.flow.*

class AnswerQuizTestController(
    private val id: Long,
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) {
    val commands: Flow<AnswerQuizTestCommand> = exercise.state.flowOf(Exercise.State::exerciseCards)
        .mapNotNull { exerciseCards: List<ExerciseCard> ->
            exerciseCards.find { it.base.id == id }
        }
        .distinctUntilChanged()
        .flatMapMerge { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
        }
        .mapTwoLatest { old: Boolean?, new: Boolean? ->
            if (old == null && new == false) {
                Vibrate
            } else {
                null
            }
        }
        .filterNotNull()

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onVariantSelected(variantIndex: Int) {
        exercise.answer(Variant(variantIndex))
        longTermStateSaver.saveStateByRegistry()
    }
}