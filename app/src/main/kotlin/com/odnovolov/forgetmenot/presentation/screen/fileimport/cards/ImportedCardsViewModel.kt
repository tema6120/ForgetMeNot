package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.*

class ImportedCardsViewModel(
    fileImporterState: FileImporter.State
) {
    val cardPrototypes: Flow<List<CardPrototype>> = fileImporterState.files[0]
        .flowOf(CardsFile::cardPrototypes)
        .flowOn(businessLogicThread)

    val numberOfSelectedCards: Flow<Int> = cardPrototypes
        .map { cardPrototype -> cardPrototype.count { it.isSelected } }
        .flowOn(businessLogicThread)
}