package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class TestingMethodViewModel(
    deckSettingsState: DeckSettings.State,
    screenState: TestingMethodScreenState
) {
    val tip: Flow<Tip?> = screenState.flowOf(TestingMethodScreenState::tip)

    val testingMethod: Flow<TestingMethod> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::testingMethod)
        }
}