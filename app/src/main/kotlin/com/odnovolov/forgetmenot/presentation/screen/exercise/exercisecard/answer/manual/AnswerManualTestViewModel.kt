package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.KoinComponent

class AnswerManualTestViewModel(
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

    val isAnswerCorrect: Flow<Boolean?> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
    }

    val hint: Flow<String?> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::hint)
    }

    val isLearned: Flow<Boolean> = exerciseCard.flatMapMerge { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }

    override fun onCleared() {
        getKoin().getScope(ANSWER_MANUAL_TEST_SCOPE_ID_PREFIX + id).close()
    }
}