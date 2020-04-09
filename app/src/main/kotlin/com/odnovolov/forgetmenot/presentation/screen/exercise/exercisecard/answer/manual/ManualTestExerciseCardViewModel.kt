package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.ManualTestExerciseCard
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual.AnswerStatus.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ManualTestExerciseCardViewModel(
    exerciseCard: ManualTestExerciseCard
) : ExerciseCardViewModel(exerciseCard) {
    val hint: Flow<String?> = exerciseCard.base.flowOf(ExerciseCard.Base::hint)

    val answerStatus: Flow<AnswerStatus> = combine(
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
        hint
    ) { isAnswerCorrect: Boolean?, hint: String? ->
        when {
            isAnswerCorrect == true -> Correct
            isAnswerCorrect == false -> Wrong
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