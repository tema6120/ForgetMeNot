package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.coroutines.flow.*

class QuizTestExerciseCardViewModel(
    private val initialExerciseCard: QuizTestExerciseCard
) {
    private val exerciseCardFlow = MutableStateFlow(initialExerciseCard)

    fun setExerciseCard(exerciseCard: QuizTestExerciseCard) {
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

    fun variantText(variantIndex: Int): Flow<String?> =
        exerciseCardFlow.flatMapLatest { exerciseCard ->
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
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

    fun variantStatus(variantIndex: Int): Flow<VariantStatus> =
        exerciseCardFlow.flatMapLatest { exerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
                .map { isAnswerCorrect: Boolean? ->
                    val isAnswered: Boolean = isAnswerCorrect != null
                    val variantCardId: Long? = exerciseCard.variants[variantIndex]?.id
                    val correctCardId: Long = exerciseCard.base.card.id
                    val selectedVariantIndex: Int? = exerciseCard.selectedVariantIndex
                    when {
                        !isAnswered -> WaitingForAnswer
                        variantCardId == correctCardId && variantIndex == selectedVariantIndex -> Correct
                        variantCardId == correctCardId -> CorrectButNotSelected
                        variantIndex == selectedVariantIndex -> Wrong
                        else -> WrongButNotSelected
                    }
                }
        }
            .distinctUntilChanged()
            .flowOn(businessLogicThread)

    val isAnswered: Flow<Boolean> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    }
        .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }
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
}