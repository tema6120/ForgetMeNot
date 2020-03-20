package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import kotlinx.serialization.Serializable

object RepetitionSettingsStateProvider {
    fun load(globalState: GlobalState): RepetitionSettings.State {
        return loadSerializable(SerializableRepetitionSettingsState.serializer())
            ?.toOriginal(globalState)
            ?: throw IllegalStateException("No RepetitionSettings.State in the Store")
    }

    fun save(state: RepetitionSettings.State) {
        val serializable: SerializableRepetitionSettingsState = state.toSerializable()
        saveSerializable(serializable, SerializableRepetitionSettingsState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableRepetitionSettingsState::class)
    }

    @Serializable
    private data class SerializableRepetitionSettingsState(
        val deckIds: List<Long>,
        val levelOfKnowledgeStart: Int,
        val levelOfKnowledgeEnd: Int
    )

    private fun RepetitionSettings.State.toSerializable() = SerializableRepetitionSettingsState(
        deckIds = decks.map { it.id },
        levelOfKnowledgeStart = levelOfKnowledgeRange.first,
        levelOfKnowledgeEnd = levelOfKnowledgeRange.last
    )

    private fun SerializableRepetitionSettingsState.toOriginal(
        globalState: GlobalState
    ): RepetitionSettings.State {
        val decks: List<Deck> = globalState.decks.filter { it.id in deckIds }
        val levelOfKnowledgeRange = levelOfKnowledgeStart..levelOfKnowledgeEnd
        return RepetitionSettings.State(decks, levelOfKnowledgeRange)
    }
}