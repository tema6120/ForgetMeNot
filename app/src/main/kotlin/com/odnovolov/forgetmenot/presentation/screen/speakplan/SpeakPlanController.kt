package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.SpeakPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState.DialogPurpose.ToAddNewSpeakEvent
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState.DialogPurpose.ToChangeAtPosition
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.soywiz.klock.seconds
import kotlinx.coroutines.flow.Flow

class SpeakPlanController(
    private val deckSettingsState: DeckSettings.State,
    private val speakPlanSettings: SpeakPlanSettings,
    private val dialogState: SpeakEventDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) {
    sealed class Command {
        object ShowCannotChangeLastSpeakQuestionMessage : Command()
        object ShowCannotChangeLastSpeakAnswerMessage : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    private val speakEvents: List<SpeakEvent>
        get() = deckSettingsState.deck.exercisePreference.speakPlan.speakEvents

    fun onSpeakEventButtonClicked(id: Long) {
        val position = speakEvents.indexOfFirst { it.id == id }
        val purpose = ToChangeAtPosition(position)
        val selectedSpeakEvent: SpeakEvent = speakEvents[position]
        val initialSelectedRadioButton: SpeakEventDialogState.SpeakEvent =
            when (selectedSpeakEvent) {
                is SpeakQuestion -> {
                    val isChangeable: Boolean = speakEvents.count { it is SpeakQuestion } > 1
                    if (!isChangeable) {
                        commandFlow.send(ShowCannotChangeLastSpeakQuestionMessage)
                        return
                    }
                    SpeakEventDialogState.SpeakEvent.SpeakQuestion
                }
                is SpeakAnswer -> {
                    val isChangeable: Boolean = speakEvents.count { it is SpeakAnswer } > 1
                    if (!isChangeable) {
                        commandFlow.send(ShowCannotChangeLastSpeakAnswerMessage)
                        return
                    }
                    SpeakEventDialogState.SpeakEvent.SpeakAnswer
                }
                is Delay -> SpeakEventDialogState.SpeakEvent.Delay
            }
        val initialInputText = if (selectedSpeakEvent is Delay) {
            selectedSpeakEvent.timeSpan.seconds.toInt().toString()
        } else {
            "2"
        }
        dialogState.run {
            dialogPurpose = purpose
            selectedRadioButton = initialSelectedRadioButton
            delayInput = initialInputText
        }
        navigator.showSpeakEventDialog()
    }

    fun onRemoveSpeakEventButtonClicked(id: Long) {
        val position = speakEvents.indexOfFirst { it.id == id }
        speakPlanSettings.removeSpeakEvent(position)
    }

    fun onAddSpeakEventButtonClicked() {
        dialogState.run {
            dialogPurpose = ToAddNewSpeakEvent
            selectedRadioButton = null
            delayInput = "2"
        }
        navigator.showSpeakEventDialog()
    }

    fun onDialogOkButtonClicked() {
        val newSpeakEvent: SpeakEvent = when (dialogState.selectedRadioButton) {
            SpeakEventDialogState.SpeakEvent.SpeakQuestion -> SpeakQuestion(generateId())
            SpeakEventDialogState.SpeakEvent.SpeakAnswer -> SpeakAnswer(generateId())
            SpeakEventDialogState.SpeakEvent.Delay -> {
                val delay: Int? = dialogState.delayInput.toIntOrNull()
                val isDelayInputValid: Boolean = delay != null && delay > 0
                if (isDelayInputValid) {
                    Delay(generateId(), delay!!.seconds)
                } else {
                    return
                }
            }
            null -> return
        }
        processNewSpeakEvent(newSpeakEvent)
    }

    fun onSpeakQuestionRadioButtonClicked() {
        dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.SpeakQuestion
        val newSpeakEvent = SpeakQuestion(generateId())
        processNewSpeakEvent(newSpeakEvent)
    }

    fun onSpeakAnswerRadioButtonClicked() {
        dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.SpeakAnswer
        val newSpeakEvent = SpeakAnswer(generateId())
        processNewSpeakEvent(newSpeakEvent)
    }

    private fun processNewSpeakEvent(speakEvent: SpeakEvent) {
        when (val purpose = dialogState.dialogPurpose) {
            ToAddNewSpeakEvent -> speakPlanSettings.addSpeakEvent(speakEvent)
            is ToChangeAtPosition ->
                speakPlanSettings.changeSpeakEvent(purpose.position, speakEvent)
            null -> return
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onDelayButtonClicked() {
        dialogState.selectedRadioButton = SpeakEventDialogState.SpeakEvent.Delay
    }

    fun onDelayInputChanged(delayInput: String) {
        dialogState.delayInput = delayInput
    }

    fun onFragmentPause() {
    }
}