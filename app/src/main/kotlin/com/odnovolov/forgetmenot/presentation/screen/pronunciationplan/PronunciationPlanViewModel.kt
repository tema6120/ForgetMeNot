package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationPlan
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class PronunciationPlanViewModel(
    deckSettingsState: DeckSettings.State,
    private val dialogState: PronunciationEventDialogState
) {
    val pronunciationEventItems: Flow<List<PronunciationEventItem>> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciationPlan)
        }
        .flatMapLatest { pronunciationPlan: PronunciationPlan ->
            pronunciationPlan.flowOf(PronunciationPlan::pronunciationEvents)
        }
        .map { pronunciationEvents: List<PronunciationEvent> ->
            val isSpeakQuestionItemRemovable: Boolean =
                pronunciationEvents.count { it == SpeakQuestion } > 1
            val isSpeakAnswerItemRemovable: Boolean =
                pronunciationEvents.count { it == SpeakAnswer } > 1
            pronunciationEvents.map { pronunciationEvent: PronunciationEvent ->
                val isRemovable = when (pronunciationEvent) {
                    SpeakQuestion -> isSpeakQuestionItemRemovable
                    SpeakAnswer -> isSpeakAnswerItemRemovable
                    is Delay -> true
                }
                PronunciationEventItem(pronunciationEvent, isRemovable)
            }
        }

    val selectedPronunciationEventType: Flow<PronunciationEventType?> =
        dialogState.flowOf(PronunciationEventDialogState::selectedRadioButton)

    val delayText: String get() = dialogState.delayInput

    val isOkButtonEnabled: Flow<Boolean> = dialogState.asFlow()
        .map { dialogState: PronunciationEventDialogState ->
            when (dialogState.selectedRadioButton) {
                PronunciationEventType.SpeakQuestion -> true
                PronunciationEventType.SpeakAnswer -> true
                PronunciationEventType.Delay -> {
                    val isDelayInputValid: Boolean = dialogState.delayInput.toIntOrNull()
                        .let { it != null && it > 0 }
                    isDelayInputValid
                }
                null -> false
            }
        }
}