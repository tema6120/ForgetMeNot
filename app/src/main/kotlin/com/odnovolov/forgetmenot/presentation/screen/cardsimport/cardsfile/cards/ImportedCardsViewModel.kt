package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.ExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.*

class ImportedCardsViewModel(
    private val cardsFileId: Long,
    cardsImporterState: CardsImporter.State
) {
    private val cardsFile: Flow<CardsFile> =
        cardsImporterState.flowOf(CardsImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    val cardPrototypes: Flow<List<CardPrototype>> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::cardPrototypes)
        }

    val hasCards: Flow<Boolean> = cardPrototypes.map { it.isNotEmpty() }

    val numberOfSelectedCards: Flow<Int> = cardPrototypes
        .map { cardPrototype -> cardPrototype.count { it.isSelected } }
        .flowOn(businessLogicThread)

    val isSelectOnlyNewButtonAvailable: Flow<Boolean> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::deckWhereToAdd)
        }
            .map { abstractDeck: AbstractDeck -> abstractDeck is ExistingDeck }
}