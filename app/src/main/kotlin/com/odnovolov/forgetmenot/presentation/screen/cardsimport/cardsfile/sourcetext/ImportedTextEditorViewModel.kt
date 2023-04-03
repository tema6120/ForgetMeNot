package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.Parser.Error
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CharsetItem
import kotlinx.coroutines.flow.*
import java.nio.charset.Charset

class ImportedTextEditorViewModel(
    private val cardsFileId: Long,
    private val cardsImporterState: CardsImporter.State
) {
    private val cardsFile: Flow<CardsFile> =
        cardsImporterState.flowOf(CardsImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    val currentCharset: Flow<Charset> = cardsFile.flatMapLatest { cardsFile: CardsFile ->
        cardsFile.flowOf(CardsFile::charset)
    }

    @OptIn(ExperimentalStdlibApi::class)
    val availableCharsets: Flow<List<CharsetItem>> = currentCharset.map { currentCharset: Charset ->
        buildList<Charset> {
            addAll(mainCharsets)
            for (charset in Charset.availableCharsets().values) {
                if (charset !in mainCharsets) {
                    add(charset)
                }
            }
        }.map { charset: Charset ->
            CharsetItem(
                charset,
                isSelected = charset == currentCharset
            )
        }
    }

    val updateTextCommand: Flow<String> = cardsFile.flatMapLatest { cardsFile: CardsFile ->
        combine(
            cardsFile.flowOf(CardsFile::charset),
            cardsFile.flowOf(CardsFile::format)
        ) { _, _ ->
            cardsFile.text
        }
    }

    val errors: Flow<List<ErrorBlock>> = cardsFile.flatMapLatest { cardsFile: CardsFile ->
        cardsFile.flowOf(CardsFile::errors)
            .debounce(50)
            .map { errors: List<Error> -> findErrorBlocks(cardsFile.text, errors) }
    }
        .flowOn(businessLogicThread)
}