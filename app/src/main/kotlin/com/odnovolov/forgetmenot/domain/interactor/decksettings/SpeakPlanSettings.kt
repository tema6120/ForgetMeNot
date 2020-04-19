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

    fun addSpeakEvent(speakEvent: SpeakEvent) {
        currentSpeakPlan.speakEvents += speakEvent
    }

    fun changeSpeakEvent(position: Int, newSpeakEvent: SpeakEvent) {
        require(position in 0..currentSpeakPlan.speakEvents.lastIndex) {
            "Invalid position: $position"
        }
        currentSpeakPlan.speakEvents.toMutableList().let { speakEvents: MutableList<SpeakEvent> ->
            speakEvents[position] = newSpeakEvent
            checkAndSetSpeakEvents(speakEvents)
        }
    }

    fun removeSpeakEvent(position: Int) {
        require(position in 0..currentSpeakPlan.speakEvents.lastIndex) {
            "Invalid position: $position"
        }
        currentSpeakPlan.speakEvents.toMutableList().let { speakEvents: MutableList<SpeakEvent> ->
            speakEvents.removeAt(position)
            checkAndSetSpeakEvents(speakEvents)
        }
    }

    private fun checkAndSetSpeakEvents(speakEvents: List<SpeakEvent>) {
        require(speakEvents.any { it is SpeakQuestion }) {
            "'SpeakPlan' must have at least one 'SpeakQuestion'"
        }
        require(speakEvents.any { it is SpeakAnswer }) {
            "'SpeakPlan' must have at least one 'SpeakAnswer'"
        }
        currentSpeakPlan.speakEvents = speakEvents
    }
}