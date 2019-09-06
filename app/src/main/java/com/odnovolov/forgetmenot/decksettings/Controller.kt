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

            RandomOrderSwitchToggled -> {
                queries.toggleRandomOrder()
            }

            PronunciationButtonClicked -> {
                with(database.pronunciationInitQueries) {
                    dropTablePronunciationState()
                    createTablePronunciationState()
                    initPronunciationState()
                    createTriggerPreventRemovalOfDefaultPronunciation()
                    createTriggerOnTryToModifyDefaultPronunciation()
                    createTriggerSetDefaultPronunciationIfNeed()
                    createTriggerOnDeletePronunciation()
                    createTriggerDeleteUnusedIndividualPronunciation()
                }
                issueOrder(NavigateToPronunciation)
            }
        }
    }
}