package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SpeakPlanViewModel(
    deckSettingsState: DeckSettings.State,
    private val dialogState: SpeakEventDialogState
) {
    val speakEventItems: Flow<List<SpeakEventItem>> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::speakPlan)
        }
        .flatMapLatest { speakPlan: SpeakPlan ->
            speakPlan.flowOf(SpeakPlan::speakEvents)
        }
        .map { speakEvents: List<SpeakEvent> ->
            val isSpeakQuestionItemRemovable: Boolean =
                speakEvents.count { it == SpeakQuestion } > 1
            val isSpeakAnswerItemRemovable: Boolean =
                speakEvents.count { it == SpeakAnswer } > 1
            speakEvents.map { speakEvent: SpeakEvent ->
                val isRemovable = when (speakEvent) {
                    SpeakQuestion -> isSpeakQuestionItemRemovable
                    SpeakAnswer -> isSpeakAnswerItemRemovable
                    is Delay -> true
                }
                SpeakEventItem(speakEvent, isRemovable)
            }
        }

    val selectedSpeakEventType: Flow<SpeakEventType?> =
        dialogState.flowOf(SpeakEventDialogState::selectedRadioButton)

    val delayText: String get() = dialogState.delayInput

    val isOkButtonEnabled: Flow<Boolean> = dialogState.asFlow()
        .map { dialogState: SpeakEventDialogState ->
            when (dialogState.selectedRadioButton) {
                SpeakEventType.SpeakQuestion -> true
                SpeakEventType.SpeakAnswer -> true
                SpeakEventType.Delay -> {
                    val isDelayInputValid: Boolean = dialogState.delayInput.toIntOrNull()
                        .let { it != null && it > 0 }
                    isDelayInputValid
                }
                null -> false
            }
        }
}