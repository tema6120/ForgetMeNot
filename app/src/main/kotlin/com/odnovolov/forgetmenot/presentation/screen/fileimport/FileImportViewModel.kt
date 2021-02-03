package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.ExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.NewDeck
import kotlinx.coroutines.flow.Flow

class FileImportViewModel(
    private val fileImporterState: FileImporter.State
) {
    val deckName: Flow<String> = fileImporterState.files[0].let { cardsFile: CardsFile ->
        when(val deckWhereToAdd = cardsFile.deckWhereToAdd) {
            is NewDeck -> deckWhereToAdd.flowOf(NewDeck::deckName)
            is ExistingDeck -> deckWhereToAdd.deck.flowOf(Deck::name)
            else -> error("Unknown type of DeckWhereToAdd")
        }
    }

    val sourceText: String get() = fileImporterState.files[0].text
}