package com.odnovolov.forgetmenot.presentation.screen.decksettings

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.checkDeckName
import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.isIndividual
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent

class DeckSettingsViewModel(
    deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettingsState: DeckSettings.State,
    private val globalState: GlobalState
) : ViewModel(), KoinComponent {
    private val currentDeck: Flow<Deck> = deckSettingsState.flowOf(DeckSettings.State::deck).share()

    private val currentExercisePreference: Flow<ExercisePreference> = currentDeck
        .flatMapLatest { deck: Deck -> deck.flowOf(Deck::exercisePreference) }
        .share()

    val deckName: Flow<String> = currentDeck.flatMapLatest { deck: Deck -> deck.flowOf(Deck::name) }

    val isRenameDeckDialogVisible: Flow<Boolean> =
        deckSettingsScreenState.flowOf(DeckSettingsScreenState::isRenameDeckDialogVisible)

    val deckNameCheckResult: Flow<NameCheckResult> =
        deckSettingsScreenState.flowOf(DeckSettingsScreenState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }

    val exercisePreference: Flow<ExercisePreference> = currentExercisePreference
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.asFlow()
        }

    val isSaveExercisePreferenceButtonEnabled: Flow<Boolean> =
        exercisePreference.map { it.isIndividual() }

    val availableExercisePreferences: Flow<List<Preset>> = combine(
        currentExercisePreference,
        globalState.flowOf(GlobalState::sharedExercisePreferences)
    ) { currentExercisePreference: ExercisePreference,
        sharedExercisePreferences: Collection<ExercisePreference>
        ->
        (sharedExercisePreferences + currentExercisePreference + ExercisePreference.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { exercisePreferences: List<ExercisePreference> ->
            val exercisePreferenceNameFlows: List<Flow<String>> = exercisePreferences
                .map { it.flowOf(ExercisePreference::name) }
            combine(exercisePreferenceNameFlows) {
                val currentExercisePreference = deckSettingsState.deck.exercisePreference
                exercisePreferences
                    .map { exercisePreference: ExercisePreference ->
                        with(exercisePreference) {
                            Preset(
                                id = id,
                                name = name,
                                isSelected = id == currentExercisePreference.id
                            )
                        }
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    val isNamePresetDialogVisible: Flow<Boolean> =
        deckSettingsScreenState.flowOf(DeckSettingsScreenState::namePresetDialogStatus)
            .map { namePresetDialogStatus: NamePresetDialogStatus ->
                namePresetDialogStatus != NamePresetDialogStatus.Invisible
            }

    val namePresetInputCheckResult: Flow<NameCheckResult> =
        deckSettingsScreenState.flowOf(DeckSettingsScreenState::typedPresetName)
            .map { typedPresetName: String ->
                checkExercisePreferenceName(typedPresetName, globalState)
            }

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

    val pronunciationIdAndName: Flow<Pronunciation> = currentExercisePreference
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

    override fun onCleared() {
        getKoin().getScope(DECK_SETTINGS_SCOPED_ID).close()
    }
}