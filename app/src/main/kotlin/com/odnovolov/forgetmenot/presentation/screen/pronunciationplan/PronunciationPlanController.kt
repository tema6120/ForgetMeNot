package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationPlanSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.DialogPurpose.ToAddNewPronunciationEvent
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.DialogPurpose.ToChangePronunciationEventAtPosition
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanController.Command
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanUiEvent.*
import com.soywiz.klock.seconds
import java.util.*

class PronunciationPlanController(
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationPlanSettings: PronunciationPlanSettings,
    private val dialogState: PronunciationEventDialogState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<PronunciationEventDialogState>
) : BaseController<PronunciationPlanUiEvent, Command>() {
    sealed class Command {
        object ShowCannotChangeLastSpeakQuestionMessage : Command()
        object ShowCannotChangeLastSpeakAnswerMessage : Command()
    }

    private val pronunciationEvents: List<PronunciationEvent>
        get() = deckSettingsState.deck.exercisePreference.pronunciationPlan.pronunciationEvents

    override fun handle(event: PronunciationPlanUiEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromPronunciationPlan {
                    HelpDiScope(HelpArticle.Repetition)
                }
            }

            is PronunciationEventButtonClicked -> {
                onPronunciationEventButtonClicked(event.position)
            }

            is RemovePronunciationEventButtonClicked -> {
                if (event.position !in 0..pronunciationEvents.lastIndex) return
                val newPronunciationEvents: List<PronunciationEvent> =
                    pronunciationEvents.toMutableList().apply { removeAt(event.position) }
                catchAndLogException {
                    pronunciationPlanSettings.setPronunciationEvents(newPronunciationEvents)
                }
            }

            AddPronunciationEventButtonClicked -> {
                dialogState.run {
                    dialogPurpose = ToAddNewPronunciationEvent
                    selectedRadioButton = null
                    delayInput = "2"
                }
                navigator.showPronunciationEventDialog()
            }

            DialogOkButtonClicked -> {
                val newPronunciationEvent: PronunciationEvent =
                    when (dialogState.selectedRadioButton) {
                        PronunciationEventType.SpeakQuestion -> SpeakQuestion
                        PronunciationEventType.SpeakAnswer -> SpeakAnswer
                        PronunciationEventType.Delay -> {
                            val delay: Int? = dialogState.delayInput.toIntOrNull()
                            if (delay != null && delay > 0) {
                                Delay(delay.seconds)
                            } else {
                                return
                            }
                        }
                        null -> return
                    }
                processNewPronunciationEvent(newPronunciationEvent)
            }

            SpeakQuestionRadioButtonClicked -> {
                dialogState.selectedRadioButton = PronunciationEventType.SpeakQuestion
                processNewPronunciationEvent(SpeakQuestion)
            }

            SpeakAnswerRadioButtonClicked -> {
                dialogState.selectedRadioButton = PronunciationEventType.SpeakAnswer
                processNewPronunciationEvent(SpeakAnswer)
            }

            DelayButtonClicked -> {
                dialogState.selectedRadioButton = PronunciationEventType.Delay
            }

            is DelayInputChanged -> {
                dialogState.delayInput = event.delayInput
            }

            is PronunciationEventItemsMoved -> {
                val newPronunciationEvents: List<PronunciationEvent> =
                    pronunciationEvents.toMutableList()
                Collections.swap(newPronunciationEvents, event.fromPosition, event.toPosition)
                pronunciationPlanSettings.setPronunciationEvents(newPronunciationEvents)
            }
        }
    }

    private fun onPronunciationEventButtonClicked(position: Int) {
        if (position !in 0..pronunciationEvents.lastIndex) return
        val purpose = ToChangePronunciationEventAtPosition(position)
        val selectedPronunciationEvent: PronunciationEvent = pronunciationEvents[position]
        val initialSelectedRadioButton: PronunciationEventType =
            when (selectedPronunciationEvent) {
                SpeakQuestion -> {
                    val isChangeable: Boolean =
                        pronunciationEvents.count { it == SpeakQuestion } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakQuestionMessage)
                        return
                    }
                    PronunciationEventType.SpeakQuestion
                }
                SpeakAnswer -> {
                    val isChangeable: Boolean = pronunciationEvents.count { it == SpeakAnswer } > 1
                    if (!isChangeable) {
                        sendCommand(ShowCannotChangeLastSpeakAnswerMessage)
                        return
                    }
                    PronunciationEventType.SpeakAnswer
                }
                is Delay -> PronunciationEventType.Delay
            }
        val initialInputText =
            if (selectedPronunciationEvent is Delay)
                selectedPronunciationEvent.timeSpan.seconds.toInt().toString()
            else
                "2"
        dialogState.run {
            dialogPurpose = purpose
            selectedRadioButton = initialSelectedRadioButton
            delayInput = initialInputText
        }
        navigator.showPronunciationEventDialog()
    }

    private fun processNewPronunciationEvent(pronunciationEvent: PronunciationEvent) {
        val newPronunciationEvents = when (val purpose = dialogState.dialogPurpose) {
            ToAddNewPronunciationEvent -> {
                pronunciationEvents + pronunciationEvent
            }
            is ToChangePronunciationEventAtPosition -> {
                if (purpose.position !in 0..pronunciationEvents.lastIndex) return
                pronunciationEvents.toMutableList().apply {
                    this[purpose.position] = pronunciationEvent
                }
            }
            null -> return
        }
        catchAndLogException {
            pronunciationPlanSettings.setPronunciationEvents(newPronunciationEvents)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}