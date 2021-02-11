package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetItem
import kotlinx.coroutines.flow.*
import java.nio.charset.Charset

class ImportedTextEditorViewModel(
    private val cardsFileId: Long,
    private val fileImporterState: FileImporter.State
) {
    private val cardsFile: Flow<CardsFile> =
        fileImporterState.flowOf(FileImporter.State::files)
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

    val sourceTextWithNewEncoding: Flow<String> = currentCharset.mapNotNull {
        fileImporterState.files.find { file: CardsFile -> file.id == cardsFileId }?.text
    }

    val errorLines: Flow<List<Int>> = cardsFile.flatMapLatest { cardsFile: CardsFile ->
        cardsFile.flowOf(CardsFile::errorLines)
    }

    val numberOfErrors: Flow<Int> = errorLines.map { errorLines: List<Int> ->
        var numberOfErrors = 0
        var previousErrorLine = -2
        for (errorLine in errorLines) {
            if (previousErrorLine + 1 != errorLine) {
                numberOfErrors++
            }
            previousErrorLine = errorLine
        }
        numberOfErrors
    }
}