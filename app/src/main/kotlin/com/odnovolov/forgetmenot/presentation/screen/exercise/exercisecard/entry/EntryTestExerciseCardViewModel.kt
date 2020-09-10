package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.AnswerStatus.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

class EntryTestExerciseCardViewModel(
    private val exerciseCard: EntryTestExerciseCard
) : ExerciseCardViewModel(exerciseCard) {
    val userInput: String get() = exerciseCard.userInput ?: ""

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

    val isInputEnabled: Flow<Boolean> = combine(
        answerStatus,
        isLearned
    ) { answerStatus: AnswerStatus, isLearned: Boolean ->
        answerStatus != Answered && !isLearned
    }

    val wrongAnswer: Flow<String?> = combine(
        exerciseCard.flowOf(EntryTestExerciseCard::userInput),
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    ) { userAnswer: String?, isAnswerCorrect: Boolean? ->
        when (isAnswerCorrect) {
            false -> userAnswer ?: ""
            else -> null
        }
    }

    val correctAnswer: Flow<String> = with(exerciseCard.base) {
        if (isReverse)
            card.flowOf(Card::question)
        else
            card.flowOf(Card::answer)
    }

    override val vibrateCommand: Flow<Unit> = exerciseCard.base
        .flowOf(ExerciseCard.Base::isAnswerCorrect)
        .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
            if (wasCorrect == null && isCorrectNow == false) Unit else null
        }
        .filterNotNull()
}