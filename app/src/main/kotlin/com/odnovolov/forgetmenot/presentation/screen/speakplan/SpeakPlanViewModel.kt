package com.odnovolov.forgetmenot.presentation.screen.speakplan

import SPEAK_PLAN_SCOPE_ID
import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState.SpeakEvent.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class SpeakPlanViewModel(
    deckSettingsState: DeckSettings.State,
    private val dialogState: SpeakEventDialogState
) : ViewModel() {
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
                speakEvents.count { it is SpeakEvent.SpeakQuestion } > 1
            val isSpeakAnswerItemRemovable: Boolean =
                speakEvents.count { it is SpeakEvent.SpeakAnswer } > 1
            speakEvents.map { speakEvent: SpeakEvent ->
                val isRemovable = when (speakEvent) {
                    is SpeakEvent.SpeakQuestion -> isSpeakQuestionItemRemovable
                    is SpeakEvent.SpeakAnswer -> isSpeakAnswerItemRemovable
                    is SpeakEvent.Delay -> true
                }
                SpeakEventItem(speakEvent, isRemovable)
            }
        }

    val selectedSpeakEvent: Flow<SpeakEventDialogState.SpeakEvent?> =
        dialogState.flowOf(SpeakEventDialogState::selectedRadioButton)

    val delayText: String = dialogState.delayInput

    val isOkButtonEnabled: Flow<Boolean> = dialogState.asFlow()
        .map { dialogState: SpeakEventDialogState ->
            when (dialogState.selectedRadioButton) {
                SpeakQuestion -> true
                SpeakAnswer -> true
                Delay -> {
                    val isDelayInputValid: Boolean = dialogState.delayInput.toIntOrNull()
                        .let { it != null && it > 0 }
                    isDelayInputValid
                }
                null -> false
            }
        }

    override fun onCleared() {
        getKoin().getScope(SPEAK_PLAN_SCOPE_ID).close()
    }
}