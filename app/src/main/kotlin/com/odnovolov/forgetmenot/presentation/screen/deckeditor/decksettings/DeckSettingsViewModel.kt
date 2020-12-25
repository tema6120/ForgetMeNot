package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class DeckSettingsViewModel(
    deckSettingsState: DeckSettings.State
) {
    private val currentExercisePreference: Flow<ExercisePreference> =
        deckSettingsState.deck.flowOf(Deck::exercisePreference).share()

    val randomOrder: Flow<Boolean> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::randomOrder)
        }

    val selectedTestMethod: Flow<TestMethod> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::testMethod)
        }

    val intervalScheme: Flow<IntervalScheme?> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::intervalScheme)
        }

    val pronunciation: Flow<Pronunciation> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }

    val isQuestionDisplayed: Flow<Boolean> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::isQuestionDisplayed)
        }

    val selectedCardReverse: Flow<CardReverse> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::cardReverse)
        }

    val pronunciationPlan: Flow<PronunciationPlan> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciationPlan)
        }

    val timeForAnswer: Flow<Int> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::timeForAnswer)
        }
}