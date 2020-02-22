package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetRenameDeckDialogText
import kotlinx.coroutines.flow.Flow

class DeckSettingsController(
    private val deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val store: Store
) {
    private var isFragmentRemoving = false
    private val commandFlow = EventFlow<DeckSettingsCommand>()
    val commands: Flow<DeckSettingsCommand> = commandFlow.get()

    fun onRenameDeckButtonClicked() {
        deckSettingsScreenState.isRenameDeckDialogVisible = true
        val deckName = deckSettings.state.deck.name
        commandFlow.send(SetRenameDeckDialogText(deckName))
    }

    fun onRenameDeckDialogTextChanged(text: String) {
        deckSettingsScreenState.typedDeckName = text
    }

    fun onRenameDeckDialogPositiveButtonClicked() {
        deckSettingsScreenState.isRenameDeckDialogVisible = false
        val newName = deckSettingsScreenState.typedDeckName
        deckSettings.renameDeck(newName)
        store.saveStateByRegistry()
    }

    fun onRenameDeckDialogNegativeButtonClicked() {
        deckSettingsScreenState.isRenameDeckDialogVisible = false
    }


    fun onSaveExercisePreferenceButtonClicked() {
        deckSettingsScreenState.namePresetDialogStatus = VisibleToMakeIndividualPresetAsShared
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onSetExercisePreferenceButtonClicked(id: Long) {
        deckSettings.setExercisePreference(id)
        store.saveStateByRegistry()
    }

    fun onRenameExercisePreferenceButtonClicked(id: Long) {
        deckSettingsScreenState.renamePresetId = id
        deckSettingsScreenState.namePresetDialogStatus = VisibleToRenameSharedPreset
        globalState.sharedExercisePreferences.find { it.id == id }
            ?.name
            ?.let { exercisePreferenceName: String ->
                commandFlow.send(SetNamePresetDialogText(exercisePreferenceName))
            }
    }

    fun onDeleteExercisePreferenceButtonClicked(id: Long) {
        deckSettings.deleteSharedExercisePreference(id)
        store.saveStateByRegistry()
    }

    fun onAddNewExercisePreferenceButtonClicked() {
        deckSettingsScreenState.namePresetDialogStatus = VisibleToCreateNewSharedPreset
        commandFlow.send(SetNamePresetDialogText(""))
    }


    fun onNamePresetDialogTextChanged(text: String) {
        deckSettingsScreenState.typedPresetName = text
    }

    fun onNamePresetPositiveDialogButtonClicked() {
        val newPresetName = deckSettingsScreenState.typedPresetName
        if (checkExercisePreferenceName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (deckSettingsScreenState.namePresetDialogStatus) {
            VisibleToMakeIndividualPresetAsShared -> {
                deckSettings.renameExercisePreference(
                    deckSettings.currentExercisePreference,
                    newPresetName
                )
            }
            VisibleToCreateNewSharedPreset -> {
                deckSettings.createNewSharedExercisePreference(newPresetName)
            }
            VisibleToRenameSharedPreset -> {
                globalState.sharedExercisePreferences
                    .find { it.id == deckSettingsScreenState.renamePresetId }
                    ?.let { exercisePreference ->
                        deckSettings.renameExercisePreference(
                            exercisePreference,
                            newPresetName
                        )
                    }
            }
            Invisible -> {
            }
        }
        deckSettingsScreenState.namePresetDialogStatus = Invisible
        store.saveStateByRegistry()
    }

    fun onNamePresetNegativeDialogButtonClicked() {
        deckSettingsScreenState.namePresetDialogStatus = Invisible
    }

    fun onRandomOrderSwitchToggled() {
        val newRandomOrder = deckSettings.currentExercisePreference.randomOrder.not()
        deckSettings.setRandomOrder(newRandomOrder)
        store.saveStateByRegistry()
    }

    fun onSelectedTestMethod(testMethod: TestMethod) {
        deckSettings.setTestMethod(testMethod)
        store.saveStateByRegistry()
    }

    fun onIntervalsButtonClicked() {
        // todo: prepare Intervals screen state
        navigator.navigateToIntervals()
    }

    fun onPronunciationButtonClicked() {
        // todo: prepare Pronunciation screen state
        navigator.navigateToPronunciation()
    }

    fun onDisplayQuestionSwitchToggled() {
        val newIsQuestionDisplayed =
            deckSettings.currentExercisePreference.isQuestionDisplayed.not()
        deckSettings.setIsQuestionDisplayed(newIsQuestionDisplayed)
        store.saveStateByRegistry()
    }

    fun onSelectedCardReverse(cardReverse: CardReverse) {
        deckSettings.setCardReverse(cardReverse)
        store.saveStateByRegistry()
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            store.deleteDeckSettingsState()
            store.deleteDeckSettingsScreenState()
        } else {
            store.save(deckSettings.state)
            store.save(deckSettingsScreenState)
        }
    }
}