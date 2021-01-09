package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.CardInversionScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardInversionScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = CardInversionScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardInversionScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val tipId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardInversionScreenState) = SerializableState(
        state.tip?.state?.id
    )

    override fun toOriginal(serializableState: SerializableState): CardInversionScreenState {
        val tip = Tip.values().find { it.state.id == serializableState.tipId }
        return CardInversionScreenState(tip)
    }
}