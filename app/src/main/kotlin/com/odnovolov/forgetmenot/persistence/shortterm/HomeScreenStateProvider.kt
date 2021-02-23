package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider.SerializableHomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSelection
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class HomeScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val fileImportStorage: FileImportStorage,
    override val key: String = HomeScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<HomeScreenState, SerializableHomeScreenState>(
    json,
    database
) {
    @Serializable
    data class SerializableHomeScreenState(
        val searchText: String,
        val deckSelection: DeckSelection?,
        val DeckIdForDeckOptionMenu: Long?,
        val fileFormatId: Long?
    )

    override val serializer = SerializableHomeScreenState.serializer()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.deckSelection,
        state.deckForDeckOptionMenu?.id,
        state.fileFormatForExport?.id
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState): HomeScreenState {
        val deck: Deck? = serializableState.DeckIdForDeckOptionMenu?.let { exportedDeckId: Long ->
            globalState.decks.first { deck -> deck.id == exportedDeckId }
        }
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
        return HomeScreenState().apply {
            searchText = serializableState.searchText
            deckSelection = serializableState.deckSelection
            deckForDeckOptionMenu = deck
            fileFormatForExport = fileFormat
        }
    }
}