package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.SpeakAnswer
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.SpeakQuestion
import com.odnovolov.forgetmenot.domain.generateId

class PronunciationPlanSettings(
    private val deckSettings: DeckSettings,
    private val globalState: GlobalState
) {
    private val currentPronunciationPlan: PronunciationPlan
        get() = deckSettings.state.deck.exercisePreference.pronunciationPlan

    fun setPronunciationPlan(pronunciationPlanId: Long) {
        when (pronunciationPlanId) {
            currentPronunciationPlan.id -> return
            PronunciationPlan.Default.id ->
                deckSettings.setPronunciationPlan(PronunciationPlan.Default)
            else -> {
                globalState.sharedPronunciationPlans
                    .find { it.id == pronunciationPlanId }
                    ?.let(deckSettings::setPronunciationPlan)
            }
        }
    }

    fun createNewSharedPronunciationPlan(name: String) {
        checkName(name)
        createNewSharedPronunciationPlanAndSetAsCurrent(name)
    }

    private fun checkName(testedName: String) {
        when (checkPronunciationPlanName(testedName, globalState)) {
            Ok -> return
            Empty -> throw IllegalArgumentException("shared PronunciationPlan name cannot be empty")
            Occupied -> throw IllegalArgumentException("$testedName is occupied")
        }
    }

    private fun createNewSharedPronunciationPlanAndSetAsCurrent(name: String) {
        val newSharedPronunciationPlan = PronunciationPlan.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedPronunciationPlan(newSharedPronunciationPlan)
        deckSettings.setPronunciationPlan(newSharedPronunciationPlan)
    }

    private fun addNewSharedPronunciationPlan(pronunciationPlan: PronunciationPlan) {
        globalState.sharedPronunciationPlans =
            (globalState.sharedPronunciationPlans + pronunciationPlan).toCopyableList()
    }

    fun renamePronunciationPlan(pronunciationPlan: PronunciationPlan, newName: String) {
        checkName(newName)
        when {
            pronunciationPlan.isDefault() -> {
                createNewSharedPronunciationPlanAndSetAsCurrent(newName)
            }
            pronunciationPlan.isIndividual() -> {
                pronunciationPlan.name = newName
                addNewSharedPronunciationPlan(pronunciationPlan)
            }
            else -> { // current PronunciationPlan is shared
                pronunciationPlan.name = newName
            }
        }
    }

    fun deleteSharedPronunciationPlan(pronunciationPlanId: Long) {
        if (pronunciationPlanId == PronunciationPlan.Default.id) return
        globalState.sharedPronunciationPlans = globalState.sharedPronunciationPlans
            .filter { sharedPronunciationPlan -> sharedPronunciationPlan.id != pronunciationPlanId }
            .toCopyableList()
        globalState.decks
            .map(Deck::exercisePreference)
            .filter { exercisePreference ->
                exercisePreference.pronunciationPlan.id == pronunciationPlanId
            }
            .distinct()
            .forEach { exercisePreference ->
                exercisePreference.pronunciationPlan = PronunciationPlan.Default
            }
        deckSettings.recheckIndividualExercisePreferences()
    }

    fun setPronunciationEvents(pronunciationEvents: List<PronunciationEvent>) {
        require(pronunciationEvents.any { it is SpeakQuestion }) {
            "'PronunciationPlan' must have at least one 'SpeakQuestion'"
        }
        require(pronunciationEvents.any { it is SpeakAnswer }) {
            "'PronunciationPlan' must have at least one 'SpeakAnswer'"
        }
        updatePronunciationPlan(
            isValueChanged = currentPronunciationPlan.pronunciationEvents != pronunciationEvents,
            createNewIndividualPronunciationPlan = {
                PronunciationPlan(
                    id = generateId(),
                    name = "",
                    pronunciationEvents = pronunciationEvents
                )
            },
            updateCurrentPronunciationPlan = {
                currentPronunciationPlan.pronunciationEvents = pronunciationEvents
            }
        )

    }

    private inline fun updatePronunciationPlan(
        isValueChanged: Boolean,
        createNewIndividualPronunciationPlan: () -> PronunciationPlan,
        updateCurrentPronunciationPlan: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentPronunciationPlan.isDefault() -> {
                val newIndividualPronunciationPlan = createNewIndividualPronunciationPlan()
                deckSettings.setPronunciationPlan(newIndividualPronunciationPlan)
            }
            currentPronunciationPlan.isIndividual() -> {
                updateCurrentPronunciationPlan()
                if (currentPronunciationPlan.shouldBeDefault()) {
                    deckSettings.setPronunciationPlan(PronunciationPlan.Default)
                }
            }
            else -> { // current PronunciationPlan is shared
                updateCurrentPronunciationPlan()
            }
        }
    }

    private fun PronunciationPlan.shallowCopy(
        id: Long,
        name: String = this.name,
        pronunciationEvents: List<PronunciationEvent> = this.pronunciationEvents
    ) = PronunciationPlan(
        id,
        name,
        pronunciationEvents
    )

    private fun PronunciationPlan.shouldBeDefault(): Boolean =
        this.shallowCopy(id = PronunciationPlan.Default.id) == PronunciationPlan.Default
}