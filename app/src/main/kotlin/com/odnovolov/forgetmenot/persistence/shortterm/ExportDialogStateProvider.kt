package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.shortterm.ExportDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.export.ExportDialogState
import com.odnovolov.forgetmenot.presentation.screen.export.Stage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExportDialogStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val fileImportStorage: FileImportStorage,
    override val key: String = ExportDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<ExportDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckIds: List<Long>,
        val fileFormatId: Long?,
        val stage: Stage
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: ExportDialogState): SerializableState {
        return SerializableState(
            state.decks.map { it.id },
            state.fileFormat?.id,
            state.stage
        )
    }

    override fun toOriginal(serializableState: SerializableState): ExportDialogState {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        val fileFormat: FileFormat? =
            if (serializableState.fileFormatId != null) {
                FileFormat.predefinedFormats.find { predefinedFileFormat: FileFormat ->
                    predefinedFileFormat.id == serializableState.fileFormatId
                } ?: fileImportStorage.customFileFormats.find { customFileFormat: FileFormat ->
                    customFileFormat.id == serializableState.fileFormatId
                }
            } else {
                null
            }
        return ExportDialogState(
            decks,
            fileFormat,
            serializableState.stage
        )
    }
}