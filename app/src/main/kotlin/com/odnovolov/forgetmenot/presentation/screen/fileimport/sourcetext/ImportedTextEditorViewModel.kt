package com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.screen.fileimport.CharsetItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.charset.Charset

class ImportedTextEditorViewModel(
    private val fileImporterState: FileImporter.State
) {
    val currentCharset: Flow<Charset> = fileImporterState.files[0].flowOf(CardsFile::charset)

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

    val sourceTextWithNewEncoding: Flow<String> = currentCharset.map {
        fileImporterState.files[0].text
    }

    val errorLines: Flow<List<Int>> = fileImporterState.files[0].flowOf(CardsFile::errorLines)

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