package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz.VariantStatus.*
import kotlinx.coroutines.flow.*

class QuizTestExerciseCardViewModel(
    private val exerciseCard: QuizTestExerciseCard
) : ExerciseCardViewModel(exerciseCard) {
    fun variantText(variantIndex: Int): Flow<String?> = run {
        val card: Card? = exerciseCard.variants.getOrNull(variantIndex)
        if (card != null) {
            if (exerciseCard.base.isReverse)
                card.flowOf(Card::question)
            else
                card.flowOf(Card::answer)
        } else {
            flowOf(null)
        }
    }

    fun variantStatus(variantIndex: Int): Flow<VariantStatus> = run {
        val correctCardId: Long = exerciseCard.base.card.id
        val variantCardId: Long? = exerciseCard.variants[variantIndex]?.id
        exerciseCard.flowOf(QuizTestExerciseCard::selectedVariantIndex)
            .map { selectedVariantIndex: Int? ->
                when {
                    selectedVariantIndex == null -> Unselected
                    variantCardId == correctCardId -> Correct
                    selectedVariantIndex == variantIndex -> Wrong
                    else -> Unaffected
                }
            }
    }

    val isAnswered: Flow<Boolean> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isAnswerCorrect)
        .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }

    val vibrateCommand: Flow<Unit> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isAnswerCorrect)
        .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
            if (wasCorrect == null && isCorrectNow == false) Unit else null
        }
        .filterNotNull()
}