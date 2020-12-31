package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.SpeakAnswer
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.SpeakQuestion
import com.odnovolov.forgetmenot.domain.entity.PronunciationPlan
import com.odnovolov.forgetmenot.domain.entity.isDefault
import com.odnovolov.forgetmenot.domain.generateId

class PronunciationPlanSettings(
    private val deckSettings: DeckSettings
) {
    private val currentPronunciationPlan: PronunciationPlan
        get() = deckSettings.state.deck.exercisePreference.pronunciationPlan

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
            else -> {
                updateCurrentPronunciationPlan()
                if (currentPronunciationPlan.shouldBeDefault()) {
                    deckSettings.setPronunciationPlan(PronunciationPlan.Default)
                }
            }
        }
    }

    private fun PronunciationPlan.shallowCopy(
        id: Long,
        pronunciationEvents: List<PronunciationEvent> = this.pronunciationEvents
    ) = PronunciationPlan(
        id,
        pronunciationEvents
    )

    private fun PronunciationPlan.shouldBeDefault(): Boolean =
        this.shallowCopy(id = PronunciationPlan.Default.id) == PronunciationPlan.Default
}