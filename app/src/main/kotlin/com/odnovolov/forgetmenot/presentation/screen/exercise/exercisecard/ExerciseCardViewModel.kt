package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import kotlinx.coroutines.flow.Flow

abstract class ExerciseCardViewModel(exerciseCard: ExerciseCard) {
    val question: Flow<String> = with(exerciseCard.base) {
        if (isReverse)
            card.flowOf(Card::answer)
        else
            card.flowOf(Card::question)
    }

    val isQuestionDisplayed: Flow<Boolean> =
        exerciseCard.base.flowOf(ExerciseCard.Base::isQuestionDisplayed)

    val isLearned: Flow<Boolean> = exerciseCard.base.card.flowOf(Card::isLearned)
}