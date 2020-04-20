package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.checkDeckName
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeckSettingsViewModel(
    deckSettingsScreenState: DeckSettingsScreenState,
    deckSettingsState: DeckSettings.State,
    private val globalState: GlobalState
) {
    private val currentExercisePreference: Flow<ExercisePreference> =
        deckSettingsState.deck.flowOf(Deck::exercisePreference).share()

    val deckName: Flow<String> = deckSettingsState.deck.flowOf(Deck::name)

    val deckNameCheckResult: Flow<NameCheckResult> =
        deckSettingsScreenState.flowOf(DeckSettingsScreenState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }

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

    val speakPlan: Flow<SpeakPlan> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::speakPlan)
        }
}