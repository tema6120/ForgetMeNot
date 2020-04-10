package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.OffTestExerciseCard
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.AnswerStatus.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OffTestExerciseCardViewModel(
    exerciseCard: OffTestExerciseCard
) : ExerciseCardViewModel(exerciseCard) {
    val hint: Flow<String?> = exerciseCard.base.flowOf(ExerciseCard.Base::hint)

    val answerStatus: Flow<AnswerStatus> = combine(
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
        hint
    ) { isAnswerCorrect: Boolean?, hint: String? ->
        when {
            isAnswerCorrect != null -> Answered
            hint != null -> UnansweredWithHint
            else -> Unanswered
        }
    }

    val answer: Flow<String> = with(exerciseCard.base) {
        if (isReverse)
            card.flowOf(Card::question)
        else
            card.flowOf(Card::answer)
    }
}