package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

interface Store {
    fun loadGlobalState(): GlobalState
    fun loadDeckReviewPreference(): DeckReviewPreference
    fun loadWalkingModePreference(): WalkingModePreference
    fun saveStateByRegistry()

    fun loadHomeScreenState(): HomeScreenState
    fun save(homeScreenState: HomeScreenState)
    fun deleteHomeScreenState()

    fun loadAddDeckState(): DeckAdder.State
    fun save(deckAdderState: DeckAdder.State)
    fun deleteAddDeckState()

    fun loadAddDeckScreenState(): AddDeckScreenState
    fun save(addDeckScreenState: AddDeckScreenState)
    fun deleteAddDeckScreenState()

    fun loadExerciseState(globalState: GlobalState): Exercise.State
    fun save(exerciseState: Exercise.State)
    fun deleteExerciseState()

    fun loadDeckSettingsState(globalState: GlobalState): DeckSettings.State
    fun save(deckSettingsState: DeckSettings.State)
    fun deleteDeckSettingsState()

    fun loadDeckSettingsScreenState(): DeckSettingsScreenState
    fun save(deckSettingsScreenState: DeckSettingsScreenState)
    fun deleteDeckSettingsScreenState()

    fun loadIntervalsScreenState(): IntervalsScreenState
    fun save(intervalsScreenState: IntervalsScreenState)
    fun deleteIntervalsScreenState()

    fun loadModifyIntervalDialogState(): ModifyIntervalDialogState
    fun save(modifyIntervalDialogState: ModifyIntervalDialogState)
    fun deleteModifyIntervalDialogState()

    fun loadPronunciationScreenState(): PronunciationScreenState
    fun save(pronunciationScreenState: PronunciationScreenState)
    fun deletePronunciationScreenState()

    fun loadRepetitionState(globalState: GlobalState): Repetition.State
    fun save(repetitionState: Repetition.State)
    fun deleteRepetitionState()
}