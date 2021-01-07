package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.CardContent.*
import kotlinx.coroutines.flow.*

class EntryTestExerciseCardViewModel(
    initialExerciseCard: EntryTestExerciseCard
) {
    private val exerciseCardFlow = MutableStateFlow(initialExerciseCard)

    fun setExerciseCard(exerciseCard: EntryTestExerciseCard) {
        exerciseCardFlow.value = exerciseCard
    }

    val cardContent: Flow<CardContent> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        combine(
            exerciseCard.base.card.flowOf(Card::question),
            exerciseCard.base.card.flowOf(Card::answer),
            exerciseCard.base.flowOf(ExerciseCard.Base::isInverted),
            exerciseCard.base.flowOf(ExerciseCard.Base::hint),
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
        ) { question: String,
            answer: String,
            isInverted: Boolean,
            hint: String?,
            isAnswerCorrect: Boolean?
            ->
            val realQuestion = if (isInverted) answer else question
            when {
                isAnswerCorrect != null -> {
                    val wrongAnswer: String? = if (isAnswerCorrect) {
                        null
                    } else {
                        val userInput = exerciseCard.userInput
                        if (userInput == null || userInput.isEmpty()) {
                            "  "
                        } else {
                            userInput
                        }
                    }
                    val correctAnswer = if (isInverted) question else answer
                    AnsweredCard(realQuestion, wrongAnswer, correctAnswer)
                }
                hint != null -> UnansweredCardWithHint(realQuestion, hint)
                else -> UnansweredCard(realQuestion)
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isQuestionDisplayed: Flow<Boolean> =
        exerciseCardFlow.flatMapLatest { exerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isQuestionDisplayed)
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
            .mapTwoLatest { wasCorrect: Boolean?, isCorrectNow: Boolean? ->
                if (wasCorrect == null && isCorrectNow == false) Unit else null
            }
            .filterNotNull()
    }
        .flowOn(businessLogicThread)

    val isLearned: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val isInputEnabled: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        combine(
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
            isLearned
        ) { isAnswerCorrect: Boolean?, isLearned: Boolean ->
            isAnswerCorrect == null && !isLearned
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val cardLabel: Flow<CardLabel?> = combine(isLearned, isExpired) { isLearned: Boolean,
                                                                      isExpired: Boolean ->
        when {
            isLearned -> CardLabel.Learned
            isExpired -> CardLabel.Expired
            else -> null
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)
}