package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class ExerciseCardViewModel(
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

    val testMethod: Flow<TestMethod> = exerciseCard.map { exerciseCard: ExerciseCard ->
        when (exerciseCard) {
            is OffTestExerciseCard -> TestMethod.Off
            is ManualTestExerciseCard -> TestMethod.Manual
            is QuizTestExerciseCard -> TestMethod.Quiz
            is EntryTestExerciseCard -> TestMethod.Entry
            else -> throw AssertionError()
        }
    }

    val question: Flow<String> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::question)
    }

    val isQuestionDisplayed: Flow<Boolean> =
        exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isQuestionDisplayed)
        }

    val isLearned: Flow<Boolean> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }

    override fun onCleared() {
        getKoin().getScope(EXERCISE_CARD_SCOPE_ID_PREFIX + id).close()
    }
}