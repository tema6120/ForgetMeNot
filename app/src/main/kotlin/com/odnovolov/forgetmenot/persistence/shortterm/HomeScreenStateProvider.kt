package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.shortterm.HomeScreenStateProvider.SerializableHomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSelection
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.soywiz.klock.DateTime
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
        val deckIdForDeckOptionMenu: Long?,
        val fileFormatId: Long?,
        val chooseDeckListDialogPurpose: ChooseDeckListDialogPurpose?,
        val deckIdRelatedToNoExerciseCardDialog: Long?,
        val timeWhenTheFirstCardWillBeAvailable: Double?
    )

    override val serializer = SerializableHomeScreenState.serializer()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.deckSelection,
        state.deckForDeckOptionMenu?.id,
        state.fileFormatForExport?.id,
        state.chooseDeckListDialogPurpose,
        state.deckRelatedToNoExerciseCardDialog?.id,
        state.timeWhenTheFirstCardWillBeAvailable?.unixMillis
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState): HomeScreenState {
        val deckForDeckOptionMenu: Deck? =
            serializableState.deckIdForDeckOptionMenu?.let { deckId: Long ->
                globalState.decks.first { deck -> deck.id == deckId }
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
        val deckRelatedToNoExerciseCardDialog: Deck? =
            serializableState.deckIdRelatedToNoExerciseCardDialog?.let { deckId: Long ->
                globalState.decks.first { deck -> deck.id == deckId }
            }
        val timeWhenTheFirstCardWillBeAvailable: DateTime? =
            serializableState.timeWhenTheFirstCardWillBeAvailable?.let(::DateTime)
        return HomeScreenState().apply {
            searchText = serializableState.searchText
            deckSelection = serializableState.deckSelection
            this.deckForDeckOptionMenu = deckForDeckOptionMenu
            fileFormatForExport = fileFormat
            chooseDeckListDialogPurpose = serializableState.chooseDeckListDialogPurpose
            this.deckRelatedToNoExerciseCardDialog = deckRelatedToNoExerciseCardDialog
            this.timeWhenTheFirstCardWillBeAvailable = timeWhenTheFirstCardWillBeAvailable
        }
    }
}