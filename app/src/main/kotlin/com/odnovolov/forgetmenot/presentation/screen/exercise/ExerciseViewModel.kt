package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class ExerciseViewModel(
    exerciseState: Exercise.State
) : ViewModel(), KoinComponent {
    val exerciseCardsIdsAtStart: List<Long> = exerciseState.exerciseCards
        .map { exerciseCard: ExerciseCard -> exerciseCard.base.id }

    val exerciseCardIds: Flow<List<Long>> = exerciseState.flowOf(Exercise.State::exerciseCards)
        .map { exerciseCards: List<ExerciseCard> ->
            exerciseCards.map { exerciseCard: ExerciseCard -> exerciseCard.base.id }
        }

    private val currentExerciseCard: Flow<ExerciseCard> = combine(
        exerciseState.flowOf(Exercise.State::exerciseCards),
        exerciseState.flowOf(Exercise.State::currentPosition)
    ) { exerciseCards: List<ExerciseCard>, currentPosition: Int ->
        exerciseCards[currentPosition]
    }
        .distinctUntilChanged()
        .share()

    val isCurrentExerciseCardLearned: Flow<Boolean> =
        currentExerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::isLearned)
        }

    val isHintButtonVisible: Flow<Boolean> =
        currentExerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
            val isQuizTestExerciseCard: Boolean = exerciseCard is QuizTestExerciseCard
            combine(
                exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
                exerciseCard.base.card.flowOf(Card::isLearned)
            ) { isAnswerCorrect: Boolean?, isLearned: Boolean ->
                !isQuizTestExerciseCard && isAnswerCorrect == null && !isLearned
            }
        }

    val levelOfKnowledgeForCurrentCard: Flow<Int?> =
        currentExerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::levelOfKnowledge)
        }

    //val isWalkingMode: Boolean = TODO()

    override fun onCleared() {
        getKoin().getScope(EXERCISE_SCOPE_ID).close()
    }
}