package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.*

class ImportedCardsViewModel(
    fileImporterState: FileImporter.State
) {
    val cardPrototypes: Flow<List<CardPrototypeItem>> = combine(
        fileImporterState.flowOf(FileImporter.State::files),
        fileImporterState.flowOf(FileImporter.State::currentPosition)
    ) { files: List<CardsFile>, currentPosition: Int ->
        files[currentPosition]
    }
        .flatMapLatest { cardsFile: CardsFile -> cardsFile.flowOf(CardsFile::parser) }
        .flatMapLatest { parser: Parser -> parser.flowOf(Parser::state) }
        .map { parserState: Parser.State ->
            parserState.cardPrototypes.mapIndexed { index: Int, cardPrototype: Parser.CardPrototype ->
                CardPrototypeItem(
                    id = index.toLong(),
                    question = parserState.text.substring(cardPrototype.questionRange),
                    answer = parserState.text.substring(cardPrototype.answerRange),
                    isSelected = true
                )
            }
        }
        .flowOn(businessLogicThread)
}