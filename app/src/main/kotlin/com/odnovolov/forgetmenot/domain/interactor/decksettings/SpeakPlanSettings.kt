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
        currentSpeakPlan.speakEvents.toMutableList().let { speakEvents: MutableList<SpeakEvent> ->
            speakEvents[position] = newSpeakEvent
            checkAndSetSpeakEvents(speakEvents)
        }
    }

    fun removeSpeakEvent(position: Int) {
        currentSpeakPlan.speakEvents.toMutableList().let { speakEvents: MutableList<SpeakEvent> ->
            speakEvents.removeAt(position)
            checkAndSetSpeakEvents(speakEvents)
        }
    }

    private fun checkAndSetSpeakEvents(speakEvents: List<SpeakEvent>) {
        val hasSpeakQuestion: Boolean = speakEvents.any { it is SpeakQuestion }
        if (!hasSpeakQuestion)
            throw IllegalStateException("'SpeakPlan' must have at least one 'SpeakQuestion'")
        val hasSpeakAnswer: Boolean = speakEvents.any { it is SpeakAnswer }
        if (!hasSpeakAnswer)
            throw IllegalStateException("'SpeakPlan' must have at least one 'SpeakAnswer'")
        currentSpeakPlan.speakEvents = speakEvents
    }
}