package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class FileFormatViewModel(
    private val cardsFileId: Long,
    fileImporterState: FileImporter.State
) {
    private val cardsFile: Flow<CardsFile> =
        fileImporterState.flowOf(FileImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    private val fileFormat: Flow<FileFormat> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::format)
        }

    val formatName: Flow<String> =
        fileFormat.map { format: FileFormat ->
            if (format.isPredefined) {
                format.name
            } else {
                "'${format.name}'"
            }
        }

    val isFmnFormatSelected: Flow<Boolean> =
        fileFormat.map { selectedFileFormat: FileFormat ->
            FileFormat.FMN_FORMAT.id == selectedFileFormat.id
        }

    val dsvFileFormatItems: Flow<List<DsvFileFormat>> =
        fileFormat.map { selectedFileFormat: FileFormat ->
            FileFormat.predefinedFormats
                .filter { predefinedFileFormat: FileFormat ->
                    when (predefinedFileFormat.extension) {
                        FileFormat.EXTENSION_CSV, FileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .map { predefinedFileFormat: FileFormat ->
                    DsvFileFormat(
                        predefinedFileFormat,
                        isSelected = predefinedFileFormat.id == selectedFileFormat.id
                    )
                }
        }
}