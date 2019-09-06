package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.NavigateToPronunciation
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.ShowRenameDeckDialog

class DeckSettingsController : BaseController<DeckSettingsEvent, DeckSettingsOrder>() {
    private val queries: DeckSettingsControllerQueries = database.deckSettingsControllerQueries

    override fun handleEvent(event: DeckSettingsEvent) {
        return when (event) {
            RenameDeckButtonClicked -> {
                issueOrder(ShowRenameDeckDialog)
            }

            RandomOrderSwitcherClicked -> {
                val exercisePreference = queries.getExercisePreference().executeAsOne()
                val newRandomOrder = exercisePreference.randomOrder.not()
                val isDefault = exercisePreference.id == 0L
                if (isDefault) {
                    queries.addExercisePreference(randomOrder = newRandomOrder)
                    queries.bindExerciseIdToDeck()
                } else {
                    val willItBeDefault = newRandomOrder && exercisePreference.pronunciationId == 0L
                    if (willItBeDefault) {
                        // Trigger will set default exercisePreferenceId for Deck automatically
                        queries.deleteExercisePreference(exercisePreference.id)
                    } else {
                        queries.setRandomOrder(newRandomOrder, exercisePreference.id)
                    }
                }
            }

            PronunciationButtonClicked -> {
                with(database.pronunciationInitQueries) {
                    dropTablePronunciationState()
                    createTablePronunciationState()
                    initPronunciationState()
                    createViewCurrentPronunciation()
                    createTriggerPreventRemovalOfDefaultPronunciation()
                    createTriggerOnTryToModifyDefaultPronunciation()
                    createTriggerSetDefaultPronunciationIfNeed()
                    createTriggerOnDeletePronunciation()
                    createTriggerDeleteUnusedIndividualPronunciation()
                }
                issueOrder(NavigateToPronunciation)
            }

            is GotPronunciation -> {
                // TODO
            }
        }
    }
}