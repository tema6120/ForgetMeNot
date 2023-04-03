package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.FileImportScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FileImportScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = CardsImportScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsImportScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val wasAskedToUseSelectedDeckForImportNextFiles: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsImportScreenState): SerializableState {
        return SerializableState(
            state.wasAskedToUseSelectedDeckForImportNextFiles
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardsImportScreenState {
        return CardsImportScreenState(
            serializableState.wasAskedToUseSelectedDeckForImportNextFiles
        )
    }
}