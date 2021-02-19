package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.FileImportScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FileImportScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = FileImportScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<FileImportScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val wasAskedToUseSelectedDeckForImportNextFiles: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: FileImportScreenState): SerializableState {
        return SerializableState(
            state.wasAskedToUseSelectedDeckForImportNextFiles
        )
    }

    override fun toOriginal(serializableState: SerializableState): FileImportScreenState {
        return FileImportScreenState(
            serializableState.wasAskedToUseSelectedDeckForImportNextFiles
        )
    }
}