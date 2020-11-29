package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.AnswerStatus.*
import kotlinx.coroutines.flow.*

class EntryTestExerciseCardViewModel(
    initialExerciseCard: EntryTestExerciseCard
) {
    private val exerciseCardFlow = MutableStateFlow(initialExerciseCard)

    fun setExerciseCard(exerciseCard: EntryTestExerciseCard) {
        exerciseCardFlow.value = exerciseCard
    }

    val isQuestionDisplayed: Flow<Boolean> =
        exerciseCardFlow.flatMapLatest { exerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isQuestionDisplayed)
        }
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

    val question: Flow<String> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        with(exerciseCard.base) {
            if (isReverse)
                card.flowOf(Card::answer)
            else
                card.flowOf(Card::question)
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val userInput: String get() = exerciseCardFlow.value.userInput ?: ""

    val hint: Flow<String?> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::hint)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val answerStatus: Flow<AnswerStatus> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    }.combine(hint) { isAnswerCorrect: Boolean?, hint: String? ->
        when {
            isAnswerCorrect != null -> Answered
            hint != null -> UnansweredWithHint
            else -> Unanswered
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val wrongAnswer: Flow<String?> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        combine(
            exerciseCard.flowOf(EntryTestExerciseCard::userInput),
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
        ) { userAnswer: String?, isAnswerCorrect: Boolean? ->
            when (isAnswerCorrect) {
                false -> userAnswer ?: ""
                else -> null
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val correctAnswer: Flow<String> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        with(exerciseCard.base) {
            if (isReverse)
                card.flowOf(Card::question)
            else
                card.flowOf(Card::answer)
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isExpired: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        combine(
            exerciseCard.base.flowOf(ExerciseCard.Base::isExpired),
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
        ) { isExpired: Boolean, isAnswerCorrect: Boolean? ->
            isExpired && isAnswerCorrect != true
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val vibrateCommand: Flow<Unit> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    }
        .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
            if (wasCorrect == null && isCorrectNow == false) Unit else null
        }
        .filterNotNull()
        .flowOn(businessLogicThread)

    val isLearned: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isInputEnabled: Flow<Boolean> = combine(
        answerStatus,
        isLearned
    ) { answerStatus: AnswerStatus, isLearned: Boolean ->
        answerStatus != Answered && !isLearned
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)
}