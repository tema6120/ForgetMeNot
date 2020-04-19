package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.SpeakAnswer
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.SpeakQuestion
import com.odnovolov.forgetmenot.domain.entity.SpeakPlan

class SpeakPlanSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    private val currentSpeakPlan: SpeakPlan
        get() = deckSettings.state.deck.exercisePreference.speakPlan

    fun setSpeakEvents(speakEvent: List<SpeakEvent>) {
        require(speakEvent.any { it is SpeakQuestion }) {
            "'SpeakPlan' must have at least one 'SpeakQuestion'"
        }
        require(speakEvent.any { it is SpeakAnswer }) {
            "'SpeakPlan' must have at least one 'SpeakAnswer'"
        }
        currentSpeakPlan.speakEvents = speakEvent
    }
}