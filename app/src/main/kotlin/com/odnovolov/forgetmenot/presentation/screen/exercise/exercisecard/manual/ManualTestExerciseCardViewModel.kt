package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.ManualTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.mapTwoLatest
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.AnswerStatus.*
import kotlinx.coroutines.flow.*

class ManualTestExerciseCardViewModel(
    initialExerciseCard: ManualTestExerciseCard
) {
    private val exerciseCardFlow = MutableStateFlow(initialExerciseCard)

    fun setExerciseCard(exerciseCard: ManualTestExerciseCard) {
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

    val hint: Flow<String?> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::hint)
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val answerStatus: Flow<AnswerStatus> = exerciseCardFlow.flatMapLatest { exerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    }.combine(hint) { isAnswerCorrect: Boolean?, hint: String? ->
        when {
            isAnswerCorrect == true -> Correct
            isAnswerCorrect == false -> Wrong
            hint != null -> UnansweredWithHint
            else -> Unanswered
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val answer: Flow<String> = exerciseCardFlow.flatMapLatest { exerciseCard ->
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
        exerciseCard.base.flowOf(ExerciseCard.Base::isExpired)
    }
        .distinctUntilChanged()
        .share()
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
        .share()
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