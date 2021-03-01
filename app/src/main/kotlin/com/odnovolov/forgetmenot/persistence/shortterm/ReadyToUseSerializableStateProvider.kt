package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class ReadyToUseSerializableStateProvider<SerializableState>(
    override val serializer: KSerializer<SerializableState>,
    json: Json,
    database: Database,
    override val key: String
) : BaseSerializableStateProvider<SerializableState, SerializableState>(
    json,
    database
) {
    override fun toOriginal(serializableState: SerializableState) = serializableState

    override fun toSerializable(state: SerializableState) = state
}