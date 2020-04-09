package com.odnovolov.forgetmenot.presentation.screen.exercise

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.NO_ACTION
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.java.KoinJavaComponent.getKoin

class ExerciseViewModel(
    private val exerciseState: Exercise.State,
    walkingModePreference: WalkingModePreference
) : ViewModel() {
    val exerciseCards: Flow<List<ExerciseCard>> =
        exerciseState.flowOf(Exercise.State::exerciseCards)

    private val currentExerciseCard: Flow<ExerciseCard> = combine(
        exerciseState.flowOf(Exercise.State::exerciseCards),
        exerciseState.flowOf(Exercise.State::currentPosition)
    ) { exerciseCards: List<ExerciseCard>, currentPosition: Int ->
        exerciseCards[currentPosition]
    }
        .distinctUntilChanged()
        .share()

    val isCurrentExerciseCardLearned: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::isLearned)
        }

    val isHintButtonVisible: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val isQuizTestExerciseCard: Boolean = exerciseCard is QuizTestExerciseCard
            combine(
                exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
                exerciseCard.base.card.flowOf(Card::isLearned)
            ) { isAnswerCorrect: Boolean?, isLearned: Boolean ->
                !isQuizTestExerciseCard && isAnswerCorrect == null && !isLearned
            }
        }

    val levelOfKnowledgeForCurrentCard: Flow<Int?> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::levelOfKnowledge)
        }

    val isWalkingMode: Boolean = exerciseState.isWalkingMode

    val needToDetectVolumeUpSinglePress =
        walkingModePreference.keyGestureMap[VOLUME_UP_SINGLE_PRESS] != NO_ACTION

    val needToDetectVolumeUpDoublePress =
        walkingModePreference.keyGestureMap[VOLUME_UP_DOUBLE_PRESS] != NO_ACTION

    val needToDetectVolumeUpLongPress =
        walkingModePreference.keyGestureMap[VOLUME_UP_LONG_PRESS] != NO_ACTION

    val needToDetectVolumeDownSinglePress =
        walkingModePreference.keyGestureMap[VOLUME_DOWN_SINGLE_PRESS] != NO_ACTION

    val needToDetectVolumeDownDoublePress =
        walkingModePreference.keyGestureMap[VOLUME_DOWN_DOUBLE_PRESS] != NO_ACTION

    val needToDetectVolumeDownLongPress =
        walkingModePreference.keyGestureMap[VOLUME_DOWN_LONG_PRESS] != NO_ACTION

    override fun onCleared() {
        getKoin().getScope(EXERCISE_SCOPE_ID).close()
    }
}