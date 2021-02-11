package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.cards

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.*

class ImportedCardsViewModel(
    private val cardsFileId: Long,
    fileImporterState: FileImporter.State
) {
    private val cardsFile: Flow<CardsFile> =
        fileImporterState.flowOf(FileImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    val cardPrototypes: Flow<List<CardPrototype>> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::cardPrototypes)
        }

    val numberOfSelectedCards: Flow<Int> = cardPrototypes
        .map { cardPrototype -> cardPrototype.count { it.isSelected } }
        .flowOn(businessLogicThread)
}