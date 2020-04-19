package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState.DialogPurpose.ToAddNewSpeakEvent
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState.DialogPurpose.ToChangeAtPosition
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanSettingsEvent.*
import com.soywiz.klock.seconds

class SpeakPlanController(
    private val deckSettingsState: DeckSettings.State,
    private val speakPlanSettings: SpeakPlanSettings,
    private val dialogState: SpeakEventDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<SpeakPlanSettingsEvent, Command>() {
    sealed class Command {
        object ShowCannotChangeLastSpeakQuestionMessage : Command()
        object ShowCannotChangeLastSpeakAnswerMessage : Command()
    }

    private val speakEvents: List<SpeakEvent>
        get() = deckSettingsState.deck.exercisePreference.speakPlan.speakEvents

    override fun handle(event: SpeakPlanSettingsEvent) {
        when (event) {
            is SpeakEventButtonClicked -> {
                onSpeakEventButtonClicked(event.id)
            }

            is RemoveSpeakEventButtonClicked -> {
                val position = speakEvents.indexOfFirst { it.id == event.id }
                catchAndLogException {
                    speakPlanSettings.removeSpeakEvent(position)
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
                    SpeakEventDialogState.SpeakEvent.SpeakQuestion -> SpeakQuestion(generateId())
                    SpeakEventDialogState.SpeakEvent.SpeakAnswer -> SpeakAnswer(generateId())
                    SpeakEventDialogState.SpeakEvent.Delay -> {
                        val delay: Int? = dialogState.delayInput.toIntOrNull()
                        if (delay != null && delay > 0) {
                            Delay(generateId(), delay.seconds)
                        } else {
                            return
                        }
                    }
                    null -> return
                }
                processNewSpeakEvent(newSpeakEvent)
            }

            SpeakQuestionRadioButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.SpeakQuestion
                val newSpeakEvent = SpeakQuestion(generateId())
                processNewSpeakEvent(newSpeakEvent)
            }

            SpeakAnswerRadioButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.SpeakAnswer
                val newSpeakEvent = SpeakAnswer(generateId())
                processNewSpeakEvent(newSpeakEvent)
            }

            DelayButtonClicked -> {
                dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.Delay
            }

            is DelayInputChanged -> {
                dialogState.delayInput = event.delayInput
            }
        }
    }

    private fun onSpeakEventButtonClicked(id: Long) {
        val position = speakEvents.indexOfFirst { it.id == id }
        if (position !in 0..speakEvents.lastIndex) return
        val purpose = ToChangeAtPosition(position)
        val selectedSpeakEvent: SpeakEvent = speakEvents[position]
        val initialSelectedRadioButton: SpeakEventDialogState.SpeakEvent =
            when (selectedSpeakEvent) {
                is SpeakQuestion -> {
                    val isChangeable: Boolean = speakEvents.count { it is SpeakQuestion } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakQuestionMessage)
                        return
                    }
                    SpeakEventDialogState.SpeakEvent.SpeakQuestion
                }
                is SpeakAnswer -> {
                    val isChangeable: Boolean = speakEvents.count { it is SpeakAnswer } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakAnswerMessage)
                        return
                    }
                    SpeakEventDialogState.SpeakEvent.SpeakAnswer
                }
                is Delay -> SpeakEventDialogState.SpeakEvent.Delay
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
        when (val purpose = dialogState.dialogPurpose) {
            ToAddNewSpeakEvent -> speakPlanSettings.addSpeakEvent(speakEvent)
            is ToChangeAtPosition -> {
                catchAndLogException {
                    speakPlanSettings.changeSpeakEvent(purpose.position, speakEvent)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}