package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsController.Command
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanDiScope

class DeckSettingsController(
    private val deckSettingsScreenState: DeckSettingsScreenState,
    private val deckSettings: DeckSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckSettingsStateProvider: ShortTermStateProvider<DeckSettings.State>,
    private val deckSettingsScreenStateProvider: ShortTermStateProvider<DeckSettingsScreenState>
) : BaseController<DeckSettingsEvent, Command>() {
    sealed class Command {
        data class ShowRenameDialogWithText(val text: String) : Command()
    }

    private val currentExercisePreference get() = deckSettings.state.deck.exercisePreference

    override fun handle(event: DeckSettingsEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                val deckName = deckSettings.state.deck.name
                sendCommand(ShowRenameDialogWithText(deckName))
            }

            is RenameDeckDialogTextChanged -> {
                deckSettingsScreenState.typedDeckName = event.text
            }

            RenameDeckDialogPositiveButtonClicked -> {
                val newName = deckSettingsScreenState.typedDeckName
                deckSettings.renameDeck(newName)
            }

            RandomOrderSwitchToggled -> {
                val newRandomOrder = !currentExercisePreference.randomOrder
                deckSettings.setRandomOrder(newRandomOrder)
            }

            is SelectedTestMethod -> {
                deckSettings.setTestMethod(event.testMethod)
            }

            IntervalsButtonClicked -> {
                navigator.navigateToIntervals {
                    IntervalsDiScope.create(PresetDialogState())
                }
            }

            PronunciationButtonClicked -> {
                navigator.navigateToPronunciation {
                    PronunciationDiScope.create(PresetDialogState())
                }
            }

            DisplayQuestionSwitchToggled -> {
                val newIsQuestionDisplayed = !currentExercisePreference.isQuestionDisplayed
                deckSettings.setIsQuestionDisplayed(newIsQuestionDisplayed)
            }

            is SelectedCardReverse -> {
                deckSettings.setCardReverse(event.cardReverse)
            }

            SpeakPlanButtonClicked -> {
                navigator.navigateToSpeakPlan {
                    SpeakPlanDiScope.create(PresetDialogState(), SpeakEventDialogState())
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckSettingsStateProvider.save(deckSettings.state)
        deckSettingsScreenStateProvider.save(deckSettingsScreenState)
    }
}