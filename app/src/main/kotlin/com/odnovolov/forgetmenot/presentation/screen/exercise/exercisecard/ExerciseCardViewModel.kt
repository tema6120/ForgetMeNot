package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

abstract class ExerciseCardViewModel(exerciseCard: ExerciseCard) {
    val question: Flow<String> = with(exerciseCard.base) {
        if (isReverse)
            card.flowOf(Card::answer)
        else
            card.flowOf(Card::question)
    }

    val isQuestionDisplayed: Flow<Boolean> =
        exerciseCard.base.flowOf(ExerciseCard.Base::isQuestionDisplayed)

    val isExpired: Flow<Boolean> = combine(
        exerciseCard.base.flowOf(ExerciseCard.Base::isExpired),
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    ) { isExpired: Boolean, isAnswerCorrect: Boolean? ->
        isExpired && isAnswerCorrect != true
    }

    open val vibrateCommand: Flow<Unit> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isExpired)
        .mapTwoLatest { wasExpired: Boolean, isExpiredNow: Boolean ->
            if (!wasExpired && isExpiredNow) Unit else null
        }
        .filterNotNull()

    val isLearned: Flow<Boolean> = exerciseCard.base.card.flowOf(Card::isLearned)
}