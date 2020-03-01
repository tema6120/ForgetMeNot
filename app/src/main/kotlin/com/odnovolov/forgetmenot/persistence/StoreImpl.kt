package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.persistence.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.persistence.serializablestate.*
import com.odnovolov.forgetmenot.persistence.walkingmodepreference.WalkingModePreferenceProvider
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class StoreImpl : Store, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Job() + dbDispatcher

    private inline fun background(crossinline block: () -> Unit) {
        launch {
            database.transaction {
                block()
            }
        }
    }

    override fun loadGlobalState(): GlobalState = GlobalStateProvider.load()

    override fun loadDeckReviewPreference(): DeckReviewPreference =
        DeckReviewPreferenceProvider.load()

    override fun loadWalkingModePreference(): WalkingModePreference =
        WalkingModePreferenceProvider.load()

    override fun saveStateByRegistry() {
        val changes = PropertyChangeRegistry.removeAll()
        background { changes.forEach(ChangeSaver::save) }
    }

    override fun loadHomeScreenState(): HomeScreenState = HomeScreenStateProvider.load()

    override fun save(homeScreenState: HomeScreenState) =
        background { HomeScreenStateProvider.save(homeScreenState) }

    override fun deleteHomeScreenState() = background { HomeScreenStateProvider.delete() }

    override fun loadAddDeckState(): AddDeckInteractor.State = AddDeckStateProvider.load()

    override fun save(addDeckInteractorState: AddDeckInteractor.State) =
        background { AddDeckStateProvider.save(addDeckInteractorState) }

    override fun deleteAddDeckState() = background { AddDeckStateProvider.delete() }

    override fun loadAddDeckScreenState(): AddDeckScreenState = AddDeckScreenStateProvider.load()

    override fun save(addDeckScreenState: AddDeckScreenState) =
        background { AddDeckScreenStateProvider.save(addDeckScreenState) }

    override fun deleteAddDeckScreenState() = background { AddDeckScreenStateProvider.delete() }

    override fun loadExerciseState(globalState: GlobalState): Exercise.State =
        ExerciseStateProvider.load(globalState)

    override fun save(exerciseState: Exercise.State) =
        background { ExerciseStateProvider.save(exerciseState) }

    override fun deleteExerciseState() = background { ExerciseStateProvider.delete() }

    override fun loadDeckSettingsState(globalState: GlobalState): DeckSettings.State =
        DeckSettingsStateProvider.load(globalState)

    override fun save(deckSettingsState: DeckSettings.State) =
        background { DeckSettingsStateProvider.save(deckSettingsState) }

    override fun deleteDeckSettingsState() = background { DeckSettingsStateProvider.delete() }

    override fun loadDeckSettingsScreenState(): DeckSettingsScreenState =
        DeckSettingsScreenStateProvider.load()

    override fun save(deckSettingsScreenState: DeckSettingsScreenState) =
        background { DeckSettingsScreenStateProvider.save(deckSettingsScreenState) }

    override fun deleteDeckSettingsScreenState() =
        background { DeckSettingsScreenStateProvider.delete() }

    override fun loadIntervalsScreenState(): IntervalsScreenState =
        IntervalsScreenStateProvider.load()

    override fun save(intervalsScreenState: IntervalsScreenState) =
        background { IntervalsScreenStateProvider.save(intervalsScreenState) }

    override fun deleteIntervalsScreenState() =
        background { IntervalsScreenStateProvider.delete() }

    override fun loadModifyIntervalDialogState(): ModifyIntervalDialogState =
        ModifyIntervalDialogStateProvider.load()

    override fun save(modifyIntervalDialogState: ModifyIntervalDialogState) =
        background { ModifyIntervalDialogStateProvider.save(modifyIntervalDialogState) }

    override fun deleteModifyIntervalDialogState() =
        background { ModifyIntervalDialogStateProvider.delete() }

    override fun loadPronunciationScreenState(): PronunciationScreenState =
        PronunciationScreenStateProvider.load()

    override fun save(pronunciationScreenState: PronunciationScreenState) =
        background { PronunciationScreenStateProvider.save(pronunciationScreenState) }

    override fun deletePronunciationScreenState() =
        background { PronunciationScreenStateProvider.delete() }

    override fun loadRepetitionState(globalState: GlobalState): Repetition.State =
        RepetitionStateProvider.load(globalState)

    override fun save(repetitionState: Repetition.State) =
        background { RepetitionStateProvider.save(repetitionState) }

    override fun deleteRepetitionState() =
        background { RepetitionStateProvider.delete() }
}