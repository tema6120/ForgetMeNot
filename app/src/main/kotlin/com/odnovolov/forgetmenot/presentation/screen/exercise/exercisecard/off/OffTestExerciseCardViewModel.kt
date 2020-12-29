package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.OffTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.CardContent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.CardContent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import kotlinx.coroutines.flow.*

class OffTestExerciseCardViewModel(
    initialExerciseCard: OffTestExerciseCard
) {
    private val exerciseCardFlow = MutableStateFlow(initialExerciseCard)

    fun setExerciseCard(exerciseCard: OffTestExerciseCard) {
        exerciseCardFlow.value = exerciseCard
    }

    val cardContent: Flow<CardContent> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        val isReverse: Boolean = exerciseCard.base.isInverted
        combine(
            exerciseCard.base.card.flowOf(Card::question),
            exerciseCard.base.card.flowOf(Card::answer),
            exerciseCard.base.flowOf(ExerciseCard.Base::hint),
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
        ) { question: String,
            answer: String,
            hint: String?,
            isAnswerCorrect: Boolean?
            ->
            val realQuestion = if (isReverse) answer else question
            val realAnswer = if (isReverse) question else answer
            when {
                isAnswerCorrect != null -> AnsweredCard(realQuestion, realAnswer)
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
        exerciseCard.base
            .flowOf(ExerciseCard.Base::isExpired)
            .mapTwoLatest { wasExpired: Boolean, isExpiredNow: Boolean ->
                if (!wasExpired && isExpiredNow) Unit else null
            }
    }
        .filterNotNull()
        .flowOn(businessLogicThread)

    val isLearned: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
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