package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class AnswerEntryTestViewModel(
    exerciseState: Exercise.State,
    private val id: Long
) : ViewModel(), KoinComponent {

    private val exerciseCard: Flow<EntryTestExerciseCard> =
        exerciseState.flowOf(Exercise.State::exerciseCards)
            .mapNotNull { exerciseCards: List<ExerciseCard> ->
                exerciseCards.find { it.base.id == id } as? EntryTestExerciseCard
            }
            .distinctUntilChanged()
            .share()

    val isAnswered: Flow<Boolean> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
            .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }
    }

    val hint: Flow<String?> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::hint)
    }

    val correctAnswer: Flow<String> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        with(exerciseCard.base) {
            if (isReverse) {
                card.flowOf(Card::question)
            } else {
                card.flowOf(Card::answer)
            }
        }
    }

    val wrongAnswer: Flow<String?> =
        exerciseCard.flatMapLatest { entryTestExerciseCard: EntryTestExerciseCard ->
            combine(
                entryTestExerciseCard.flowOf(EntryTestExerciseCard::userAnswer),
                entryTestExerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
            ) { userAnswer: String?, isAnswerCorrect: Boolean? ->
                if (isAnswerCorrect == false) userAnswer else null
            }
        }

    val isLearned: Flow<Boolean> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }

    override fun onCleared() {
        getKoin().getScope(ANSWER_ENTRY_TEST_SCOPE_ID_PREFIX + id).close()
    }
}