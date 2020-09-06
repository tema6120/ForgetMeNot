package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.DialogPurpose.ToAddNewSpeakEvent
import com.odnovolov.forgetmenot.presentation.screen.speakplan.DialogPurpose.ToChangeSpeakEventAtPosition
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.*
import com.soywiz.klock.seconds
import java.util.*

class SpeakPlanController(
    private val deckSettingsState: DeckSettings.State,
    private val speakPlanSettings: SpeakPlanSettings,
    private val dialogState: SpeakEventDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<SpeakEventDialogState>
) : BaseController<SpeakPlanUiEvent, Command>() {
    sealed class Command {
        object ShowCannotChangeLastSpeakQuestionMessage : Command()
        object ShowCannotChangeLastSpeakAnswerMessage : Command()
    }

    private val speakEvents: List<SpeakEvent>
        get() = deckSettingsState.deck.exercisePreference.speakPlan.speakEvents

    override fun handle(event: SpeakPlanUiEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromSpeakPlan {
                    HelpDiScope(HelpArticle.Repetition)
                }
            }

            is SpeakEventButtonClicked -> {
                onSpeakEventButtonClicked(event.position)
            }

            is RemoveSpeakEventButtonClicked -> {
                if (event.position !in 0..speakEvents.lastIndex) return
                val newSpeakEvents: List<SpeakEvent> = speakEvents.toMutableList().apply {
                    removeAt(event.position)
                }
                catchAndLogException {
                    speakPlanSettings.setSpeakEvents(newSpeakEvents)
                }
            }

            AddSpeakEventButtonClicked -> {
                dialogState.run {
                    dialogPurpose = ToAddNewSpeakEvent
                    selectedRadioButton = null
                    delayInput = "2"
                }
                navigator.showSpeakEventDialog()
            }

            DialogOkButtonClicked -> {
                val newSpeakEvent: SpeakEvent = when (dialogState.selectedRadioButton) {
                    SpeakEventType.SpeakQuestion -> SpeakQuestion
                    SpeakEventType.SpeakAnswer -> SpeakAnswer
                    SpeakEventType.Delay -> {
                        val delay: Int? = dialogState.delayInput.toIntOrNull()
                        if (delay != null && delay > 0) {
                            Delay(delay.seconds)
                        } else {
                            return
                        }
                    }
                    null -> return
                }
                processNewSpeakEvent(newSpeakEvent)
            }

            SpeakQuestionRadioButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventType.SpeakQuestion
                processNewSpeakEvent(SpeakQuestion)
            }

            SpeakAnswerRadioButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventType.SpeakAnswer
                processNewSpeakEvent(SpeakAnswer)
            }

            DelayButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventType.Delay
            }

            is DelayInputChanged -> {
                dialogState.delayInput = event.delayInput
            }

            is SpeakEventItemsMoved -> {
                val newSpeakEvents: List<SpeakEvent> = speakEvents.toMutableList()
                Collections.swap(newSpeakEvents, event.fromPosition, event.toPosition)
                speakPlanSettings.setSpeakEvents(newSpeakEvents)
            }
        }
    }

    private fun onSpeakEventButtonClicked(position: Int) {
        if (position !in 0..speakEvents.lastIndex) return
        val purpose = ToChangeSpeakEventAtPosition(position)
        val selectedSpeakEvent: SpeakEvent = speakEvents[position]
        val initialSelectedRadioButton: SpeakEventType =
            when (selectedSpeakEvent) {
                SpeakQuestion -> {
                    val isChangeable: Boolean = speakEvents.count { it == SpeakQuestion } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakQuestionMessage)
                        return
                    }
                    SpeakEventType.SpeakQuestion
                }
                SpeakAnswer -> {
                    val isChangeable: Boolean = speakEvents.count { it == SpeakAnswer } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakAnswerMessage)
                        return
                    }
                    SpeakEventType.SpeakAnswer
                }
                is Delay -> SpeakEventType.Delay
            }
        val initialInputText =
            if (selectedSpeakEvent is Delay)
                selectedSpeakEvent.timeSpan.seconds.toInt().toString()
            else
                "2"
        dialogState.run {
            dialogPurpose = purpose
            selectedRadioButton = initialSelectedRadioButton
            delayInput = initialInputText
        }
        navigator.showSpeakEventDialog()
    }

    private fun processNewSpeakEvent(speakEvent: SpeakEvent) {
        val newSpeakEvents = when (val purpose = dialogState.dialogPurpose) {
            ToAddNewSpeakEvent -> {
                speakEvents + speakEvent
            }
            is ToChangeSpeakEventAtPosition -> {
                if (purpose.position !in 0..speakEvents.lastIndex) return
                speakEvents.toMutableList().apply {
                    this[purpose.position] = speakEvent
                }
            }
            null -> return
        }
        catchAndLogException {
            speakPlanSettings.setSpeakEvents(newSpeakEvents)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}