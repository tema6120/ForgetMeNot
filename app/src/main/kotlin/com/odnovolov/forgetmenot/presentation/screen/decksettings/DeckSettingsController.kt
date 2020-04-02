package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.checkExercisePreferenceName
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetRenameDeckDialogText
import com.odnovolov.forgetmenot.presentation.screen.intervals.INTERVALS_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsViewModel
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PRONUNCIATION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.scope.Scope
import org.koin.java.KoinJavaComponent.getKoin

class DeckSettingsController(
    private val deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckSettingsStateProvider: ShortTermStateProvider<DeckSettings.State>,
    private val deckSettingsScreenStateProvider: ShortTermStateProvider<DeckSettingsScreenState>
) {
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
        longTermStateSaver.saveStateByRegistry()
    }

    fun onRenameDeckDialogNegativeButtonClicked() {
        deckSettingsScreenState.isRenameDeckDialogVisible = false
    }


    fun onSaveExercisePreferenceButtonClicked() {
        deckSettingsScreenState.namePresetDialogStatus = VisibleToMakeIndividualPresetAsShared
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onSetExercisePreferenceButtonClicked(exercisePreferenceId: Long) {
        deckSettings.setExercisePreference(exercisePreferenceId)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onRenameExercisePreferenceButtonClicked(exercisePreferenceId: Long) {
        deckSettingsScreenState.renamePresetId = exercisePreferenceId
        deckSettingsScreenState.namePresetDialogStatus = VisibleToRenameSharedPreset
        globalState.sharedExercisePreferences.find { it.id == exercisePreferenceId }
            ?.name
            ?.let { exercisePreferenceName: String ->
                commandFlow.send(SetNamePresetDialogText(exercisePreferenceName))
            }
    }

    fun onDeleteExercisePreferenceButtonClicked(exercisePreferenceId: Long) {
        deckSettings.deleteSharedExercisePreference(exercisePreferenceId)
        longTermStateSaver.saveStateByRegistry()
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
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNamePresetNegativeDialogButtonClicked() {
        deckSettingsScreenState.namePresetDialogStatus = Invisible
    }

    fun onRandomOrderSwitchToggled() {
        val newRandomOrder = deckSettings.currentExercisePreference.randomOrder.not()
        deckSettings.setRandomOrder(newRandomOrder)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSelectedTestMethod(testMethod: TestMethod) {
        deckSettings.setTestMethod(testMethod)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onIntervalsButtonClicked() {
        val koinScope: Scope = getKoin().createScope<IntervalsViewModel>(INTERVALS_SCOPE_ID)
        koinScope.declare(IntervalsScreenState(), override = true)
        navigator.navigateToIntervals()
    }

    fun onPronunciationButtonClicked() {
        val koinScope: Scope = getKoin().createScope<PronunciationViewModel>(PRONUNCIATION_SCOPE_ID)
        koinScope.declare(PronunciationScreenState(), override = true)
        navigator.navigateToPronunciation()
    }

    fun onDisplayQuestionSwitchToggled() {
        val newIsQuestionDisplayed =
            deckSettings.currentExercisePreference.isQuestionDisplayed.not()
        deckSettings.setIsQuestionDisplayed(newIsQuestionDisplayed)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSelectedCardReverse(cardReverse: CardReverse) {
        deckSettings.setCardReverse(cardReverse)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSpeakPlanButtonClicked() {
        navigator.navigateToSpeakPlan()
    }

    fun onFragmentPause() {
        deckSettingsStateProvider.save(deckSettings.state)
        deckSettingsScreenStateProvider.save(deckSettingsScreenState)
    }
}