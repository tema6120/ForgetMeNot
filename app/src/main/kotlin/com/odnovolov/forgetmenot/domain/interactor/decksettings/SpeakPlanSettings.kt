package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.SpeakAnswer
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.SpeakQuestion
import com.odnovolov.forgetmenot.domain.generateId

class SpeakPlanSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    private val currentSpeakPlan: SpeakPlan
        get() = deckSettings.state.deck.exercisePreference.speakPlan

    fun setSpeakPlan(speakPlanId: Long) {
        when (speakPlanId) {
            currentSpeakPlan.id -> return
            SpeakPlan.Default.id -> deckSettings.setSpeakPlan(SpeakPlan.Default)
            else -> {
                globalState.sharedSpeakPlans
                    .find { it.id == speakPlanId }
                    ?.let(deckSettings::setSpeakPlan)
            }
        }
    }

    fun createNewSharedSpeakPlan(name: String) {
        checkName(name)
        createNewSharedSpeakPlanAndSetAsCurrent(name)
    }

    private fun checkName(testedName: String) {
        when (checkSpeakPlanName(testedName, globalState)) {
            Ok -> return
            Empty -> throw IllegalArgumentException("shared SpeakPlan name cannot be empty")
            Occupied -> throw IllegalArgumentException("$testedName is occupied")
        }
    }

    private fun createNewSharedSpeakPlanAndSetAsCurrent(name: String) {
        val newSharedSpeakPlan = SpeakPlan.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedSpeakPlan(newSharedSpeakPlan)
        deckSettings.setSpeakPlan(newSharedSpeakPlan)
    }

    private fun addNewSharedSpeakPlan(speakPlan: SpeakPlan) {
        globalState.sharedSpeakPlans = (globalState.sharedSpeakPlans + speakPlan).toCopyableList()
    }

    fun renameSpeakPlan(speakPlan: SpeakPlan, newName: String) {
        checkName(newName)
        when {
            speakPlan.isDefault() -> {
                createNewSharedSpeakPlanAndSetAsCurrent(newName)
            }
            speakPlan.isIndividual() -> {
                speakPlan.name = newName
                addNewSharedSpeakPlan(speakPlan)
            }
            else -> { // current SpeakPlan is shared
                speakPlan.name = newName
            }
        }
    }

    fun deleteSharedSpeakPlan(speakPlanId: Long) {
        if (speakPlanId == SpeakPlan.Default.id) return
        globalState.sharedSpeakPlans = globalState.sharedSpeakPlans
            .filter { sharedSpeakPlan -> sharedSpeakPlan.id != speakPlanId }
            .toCopyableList()
        globalState.decks
            .map(Deck::exercisePreference)
            .filter { exercisePreference -> exercisePreference.speakPlan.id == speakPlanId }
            .distinct()
            .forEach { exercisePreference ->
                exercisePreference.speakPlan = SpeakPlan.Default
            }
        deckSettings.recheckIndividualExercisePreferences()
    }

    fun setSpeakEvents(speakEvents: List<SpeakEvent>) {
        require(speakEvents.any { it is SpeakQuestion }) {
            "'SpeakPlan' must have at least one 'SpeakQuestion'"
        }
        require(speakEvents.any { it is SpeakAnswer }) {
            "'SpeakPlan' must have at least one 'SpeakAnswer'"
        }
        updateSpeakPlan(
            isValueChanged = currentSpeakPlan.speakEvents != speakEvents,
            createNewIndividualSpeakPlan = {
                SpeakPlan(
                    id = generateId(),
                    name = "",
                    speakEvents = speakEvents
                )
            },
            updateCurrentSpeakPlan = {
                currentSpeakPlan.speakEvents = speakEvents
            }
        )

    }

    private inline fun updateSpeakPlan(
        isValueChanged: Boolean,
        createNewIndividualSpeakPlan: () -> SpeakPlan,
        updateCurrentSpeakPlan: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentSpeakPlan.isDefault() -> {
                val newIndividualSpeakPlan = createNewIndividualSpeakPlan()
                deckSettings.setSpeakPlan(newIndividualSpeakPlan)
            }
            currentSpeakPlan.isIndividual() -> {
                updateCurrentSpeakPlan()
                if (currentSpeakPlan.shouldBeDefault()) {
                    deckSettings.setSpeakPlan(SpeakPlan.Default)
                }
            }
            else -> { // current SpeakPlan is shared
                updateCurrentSpeakPlan()
            }
        }
    }

    private fun SpeakPlan.shallowCopy(
        id: Long,
        name: String = this.name,
        speakEvents: List<SpeakEvent> = this.speakEvents
    ) = SpeakPlan(
        id,
        name,
        speakEvents
    )

    private fun SpeakPlan.shouldBeDefault(): Boolean =
        this.shallowCopy(id = SpeakPlan.Default.id) == SpeakPlan.Default
}