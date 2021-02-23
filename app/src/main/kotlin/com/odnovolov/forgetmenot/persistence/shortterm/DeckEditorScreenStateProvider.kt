package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.shortterm.DeckEditorScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckEditorScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val fileImportStorage: FileImportStorage,
    override val key: String = DeckEditorScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckEditorScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long,
        val tabs: DeckEditorTabs,
        val fileFormatId: Long?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckEditorScreenState) = SerializableState(
        state.deck.id,
        state.tabs,
        state.fileFormatForExport?.id
    )

    override fun toOriginal(serializableState: SerializableState): DeckEditorScreenState {
        val deck: Deck = globalState.decks.first { it.id == serializableState.deckId }
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
        return DeckEditorScreenState(deck, serializableState.tabs, fileFormat)
    }
}