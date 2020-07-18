package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

class ExerciseViewModel(
    exerciseState: Exercise.State,
    speakerImplState: SpeakerImpl.State,
    walkingModePreference: WalkingModePreference,
    globalState: GlobalState
) {
    val isWalkingModeEnabled: Flow<Boolean> = globalState.flowOf(GlobalState::isWalkingModeEnabled)

    val exerciseCards: Flow<List<ExerciseCard>> =
        exerciseState.flowOf(Exercise.State::exerciseCards)

    val currentPosition: Int = exerciseState.currentPosition

    private val currentExerciseCard: Flow<ExerciseCard> = combine(
        exerciseCards,
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

    val isSpeaking: Flow<Boolean> = speakerImplState.flowOf(SpeakerImpl.State::isSpeaking)

    val isHintAccessible: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            val isQuizTestExerciseCard: Boolean = exerciseCard is QuizTestExerciseCard
            combine(
                exerciseCard.base.flowOf(ExerciseCard.Base::isAnswerCorrect),
                exerciseCard.base.card.flowOf(Card::isLearned)
            ) { isAnswerCorrect: Boolean?, isLearned: Boolean ->
                !isQuizTestExerciseCard && isAnswerCorrect == null && !isLearned
            }
        }

    val timeLeft: Flow<Int> = currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
        exerciseCard.base.flowOf(ExerciseCard.Base::timeLeft)
    }

    val levelOfKnowledgeForCurrentCard: Flow<Int> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.card.flowOf(Card::levelOfKnowledge)
        }

    val isLevelOfKnowledgeEditedManually: Flow<Boolean> =
        currentExerciseCard.flatMapLatest { exerciseCard: ExerciseCard ->
            exerciseCard.base.flowOf(ExerciseCard.Base::isLevelOfKnowledgeEditedManually)
        }

    val keyGestureMap: Flow<Map<KeyGesture, KeyGestureAction>> =
        walkingModePreference.flowOf(WalkingModePreference::keyGestureMap)
}