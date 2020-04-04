package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsCommand.SetRenameDeckDialogText
import kotlinx.coroutines.flow.Flow

class DeckSettingsController(
    private val deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettings: DeckSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckSettingsStateProvider: ShortTermStateProvider<DeckSettings.State>,
    private val deckSettingsScreenStateProvider: ShortTermStateProvider<DeckSettingsScreenState>
) {
    private val commandFlow = EventFlow<DeckSettingsCommand>()
    val commands: Flow<DeckSettingsCommand> = commandFlow.get()
    private val currentExercisePreference get() = deckSettings.state.deck.exercisePreference

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

    fun onRandomOrderSwitchToggled() {
        val newRandomOrder = !currentExercisePreference.randomOrder
        deckSettings.setRandomOrder(newRandomOrder)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSelectedTestMethod(testMethod: TestMethod) {
        deckSettings.setTestMethod(testMethod)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onIntervalsButtonClicked() {
        navigator.navigateToIntervals()
    }

    fun onPronunciationButtonClicked() {
        navigator.navigateToPronunciation()
    }

    fun onDisplayQuestionSwitchToggled() {
        val newIsQuestionDisplayed = !currentExercisePreference.isQuestionDisplayed
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