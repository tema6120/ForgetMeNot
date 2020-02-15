package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.mapToOneNotNull
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardViewModelQueries
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class AnswerOffTestViewModel(
    exerciseState: Exercise.State,
    private val id: Long
) : ViewModel(), KoinComponent {

    private val exerciseCard: Flow<ExerciseCard> =
        exerciseState.flowOf(Exercise.State::exerciseCards)
            .mapNotNull { exerciseCards: List<ExerciseCard> ->
                exerciseCards.find { it.base.id == id }
            }
            .distinctUntilChanged()
            .share()

    val answer: Flow<String> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::answer)
    }

    val isAnswered: Flow<Boolean> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
            .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }
    }

    val hint: Flow<String?> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::hint)
    }

    val isLearned: Flow<Boolean> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }

    override fun onCleared() {
        getKoin().getScope(ANSWER_OFF_TEST_SCOPE_ID_PREFIX + id).close()
    }
}