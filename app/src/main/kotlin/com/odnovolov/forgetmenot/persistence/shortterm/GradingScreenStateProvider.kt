package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.GradingScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState.DialogPurpose
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GradingScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = GradingScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<GradingScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?,
        val dialogPurpose: DialogPurpose?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: GradingScreenState): SerializableState {
        return SerializableState(
            state.tip?.state?.id,
            state.dialogPurpose
        )
    }

    override fun toOriginal(serializableState: SerializableState): GradingScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return GradingScreenState(
            tip,
            serializableState.dialogPurpose
        )
    }
}