package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PronunciationScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = PronunciationScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<PronunciationScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: PronunciationScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): PronunciationScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return PronunciationScreenState(tip)
    }
}