package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckContentViewModel(
    deckEditorScreenState: DeckEditorScreenState,
    fileImportStorage: FileImportStorage
) {
    val cards: Flow<List<Card>> = deckEditorScreenState.deck.flowOf(Deck::cards)

    val dsvFileFormats: Flow<List<FileFormat>> = fileImportStorage
        .flowOf(FileImportStorage::customFileFormats)
        .map { customFileFormats: CopyableCollection<FileFormat> ->
            FileFormat.predefinedFormats
                .filter { predefinedFileFormat: FileFormat ->
                    when (predefinedFileFormat.extension) {
                        FileFormat.EXTENSION_CSV, FileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .plus(customFileFormats)
        }
}