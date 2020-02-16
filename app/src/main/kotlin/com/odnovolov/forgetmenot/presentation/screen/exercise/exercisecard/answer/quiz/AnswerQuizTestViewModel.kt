package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class AnswerQuizTestViewModel(
    exerciseState: Exercise.State,
    private val id: Long
) : ViewModel(), KoinComponent {

    private val exerciseCard: Flow<QuizTestExerciseCard> =
        exerciseState.flowOf(Exercise.State::exerciseCards)
            .mapNotNull { exerciseCards: List<ExerciseCard> ->
                exerciseCards.find { it.base.id == id } as? QuizTestExerciseCard
            }
            .distinctUntilChanged()
            .share()

    val variant1: Flow<String> = variantTextFlow(0)
    val variant2: Flow<String> = variantTextFlow(1)
    val variant3: Flow<String> = variantTextFlow(2)
    val variant4: Flow<String> = variantTextFlow(3)

    private fun variantTextFlow(variantIndex: Int): Flow<String> {
        return exerciseCard.flatMapLatest { quizTestExerciseCard: QuizTestExerciseCard ->
            val card: Card? = quizTestExerciseCard.variants.getOrNull(variantIndex)
            val isReverse: Boolean = quizTestExerciseCard.base.isReverse
            if (card != null) {
                if (isReverse) {
                    card.flowOf(Card::question)
                } else {
                    card.flowOf(Card::answer)
                }
            } else {
                emptyFlow()
            }
        }
    }

    val variant1Status: Flow<VariantStatus> = variantStatusFlow(0)
    val variant2Status: Flow<VariantStatus> = variantStatusFlow(1)
    val variant3Status: Flow<VariantStatus> = variantStatusFlow(2)
    val variant4Status: Flow<VariantStatus> = variantStatusFlow(3)

    private fun variantStatusFlow(variantIndex: Int): Flow<VariantStatus> {
        return exerciseCard.flatMapLatest { quizTestExerciseCard: QuizTestExerciseCard ->
            val correctCardId: Long = quizTestExerciseCard.base.card.id
            val variantCardId: Long? = quizTestExerciseCard.variants[variantIndex]?.id
            quizTestExerciseCard.flowOf(QuizTestExerciseCard::selectedVariantIndex)
                .map { selectedVariantIndex: Int? ->
                    when {
                        selectedVariantIndex == null -> VariantStatus.Unselected
                        variantCardId == correctCardId -> VariantStatus.Correct
                        selectedVariantIndex == variantIndex -> VariantStatus.Wrong
                        else -> VariantStatus.Unselected
                    }
                }
        }
    }

    val isAnswered: Flow<Boolean> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect)
            .map { isAnswerCorrect: Boolean? -> isAnswerCorrect != null }
    }

    val isLearned: Flow<Boolean> = exerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.card.flowOf(Card::isLearned)
    }

    override fun onCleared() {
        getKoin().getScope(ANSWER_QUIZ_TEST_SCOPE_ID_PREFIX + id).close()
    }
}