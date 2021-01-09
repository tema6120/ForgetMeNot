package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class QuestionDisplayViewModel(
    deckSettingsState: DeckSettings.State,
    screenState: QuestionDisplayScreenState
) {
    val tip: Flow<Tip?> = screenState.flowOf(QuestionDisplayScreenState::tip)

    val isQuestionDisplayed: Flow<Boolean> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::isQuestionDisplayed)
        }
}