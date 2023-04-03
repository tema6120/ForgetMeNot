package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImportStorage
import com.odnovolov.forgetmenot.persistence.shortterm.ExportDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.cardsexport.CardsExportDialogState
import com.odnovolov.forgetmenot.presentation.screen.cardsexport.Stage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ExportDialogStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val cardsImportStorage: CardsImportStorage,
    override val key: String = CardsExportDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsExportDialogState, SerializableState>(
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

    override fun toSerializable(state: CardsExportDialogState): SerializableState {
        return SerializableState(
            state.decks.map { it.id },
            state.fileFormat?.id,
            state.stage
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardsExportDialogState {
        val decks: List<Deck> = globalState.decks.filter { it.id in serializableState.deckIds }
        val fileFormat: CardsFileFormat? =
            if (serializableState.fileFormatId != null) {
                CardsFileFormat.predefinedFormats.find { predefinedFileFormat: CardsFileFormat ->
                    predefinedFileFormat.id == serializableState.fileFormatId
                } ?: cardsImportStorage.customFileFormats.find { customFileFormat: CardsFileFormat ->
                    customFileFormat.id == serializableState.fileFormatId
                }
            } else {
                null
            }
        return CardsExportDialogState(
            decks,
            fileFormat,
            serializableState.stage
        )
    }
}