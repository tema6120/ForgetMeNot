package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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

    fun variantStatus(variantIndex: Int): Flow<VariantStatus> =
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
            .map { isAnswerCorrect: Boolean? ->
                val isAnswered: Boolean = isAnswerCorrect != null
                val variantCardId: Long? = exerciseCard.variants[variantIndex]?.id
                val correctCardId: Long = exerciseCard.base.card.id
                val selectedVariantIndex: Int? = exerciseCard.selectedVariantIndex
                when {
                    !isAnswered -> Unselected
                    variantCardId == correctCardId -> Correct
                    variantIndex == selectedVariantIndex -> Wrong
                    else -> Unselected
                }
            }

    val isAnswered: Flow<Boolean> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isAnswerCorrect)
        .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }

    override val vibrateCommand: Flow<Unit> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isAnswerCorrect)
        .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
            if (wasCorrect == null && isCorrectNow == false) Unit else null
        }
        .filterNotNull()
}