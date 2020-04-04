package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsController.Command.ShowRenameDialogWithText
import kotlinx.coroutines.flow.Flow

class DeckSettingsController(
    private val deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettings: DeckSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckSettingsStateProvider: ShortTermStateProvider<DeckSettings.State>,
    private val deckSettingsScreenStateProvider: ShortTermStateProvider<DeckSettingsScreenState>
) {
    sealed class Command {
        data class ShowRenameDialogWithText(val text: String) : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()
    private val currentExercisePreference get() = deckSettings.state.deck.exercisePreference

    fun onRenameDeckButtonClicked() {
        val deckName = deckSettings.state.deck.name
        commandFlow.send(ShowRenameDialogWithText(deckName))
    }

    fun onRenameDeckDialogTextChanged(text: String) {
        deckSettingsScreenState.typedDeckName = text
    }

    fun onRenameDeckDialogPositiveButtonClicked() {
        val newName = deckSettingsScreenState.typedDeckName
        deckSettings.renameDeck(newName)
        longTermStateSaver.saveStateByRegistry()
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

    fun performSaving() {
        deckSettingsStateProvider.save(deckSettings.state)
        deckSettingsScreenStateProvider.save(deckSettingsScreenState)
    }
}