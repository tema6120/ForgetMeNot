package com.odnovolov.forgetmenot.presentation.screen.home.choosepreset

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSelection
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ChoosePresetViewModel(
    screenState: HomeScreenState,
    globalState: GlobalState
) {
    val presets: Flow<List<SelectablePreset>> = combine(
        globalState.flowOf(GlobalState::decks),
        screenState.flowOf(HomeScreenState::deckSelection),
        globalState.flowOf(GlobalState::sharedExercisePreferences)
    ) { decks: Collection<Deck>,
        deckSelection: DeckSelection?,
        sharedExercisePreferences: Collection<ExercisePreference>
        ->
        val selectedDeckIds = deckSelection?.selectedDeckIds ?: return@combine emptyList()
        var theOnlyExercisePreferenceUsedForSelectedDecks: ExercisePreference? = null
        for (deck in decks) {
            if (deck.id !in selectedDeckIds) continue
            when {
                theOnlyExercisePreferenceUsedForSelectedDecks == null -> {
                    theOnlyExercisePreferenceUsedForSelectedDecks = deck.exercisePreference
                }
                deck.exercisePreference.id != theOnlyExercisePreferenceUsedForSelectedDecks.id -> {
                    theOnlyExercisePreferenceUsedForSelectedDecks = null
                    break
                }
            }
        }
        val presets = ArrayList<SelectablePreset>()
        val defaultPreset = SelectablePreset(
            ExercisePreference.Default,
            isSelected = ExercisePreference.Default.id == theOnlyExercisePreferenceUsedForSelectedDecks?.id
        )
        presets.add(defaultPreset)
        val sharedPresets =
            sharedExercisePreferences.map { exercisePreference: ExercisePreference ->
                SelectablePreset(
                    exercisePreference,
                    isSelected = exercisePreference.id == theOnlyExercisePreferenceUsedForSelectedDecks?.id
                )
            }
        presets.addAll(sharedPresets)
        presets
    }
}