package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import kotlinx.coroutines.flow.*

class FileFormatViewModel(
    private val cardsFileId: Long,
    fileImporterState: FileImporter.State,
    fileImportStorage: FileImportStorage
) {
    private val cardsFile: Flow<CardsFile> =
        fileImporterState.flowOf(FileImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    private val currentFileFormat: Flow<FileFormat> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::format)
        }

    val formatName: Flow<String> =
        currentFileFormat.map { format: FileFormat ->
            if (format.isPredefined) {
                format.name
            } else {
                "'${format.name}'"
            }
        }

    val isFmnFormatSelected: Flow<Boolean> =
        currentFileFormat.map { selectedFileFormat: FileFormat ->
            FileFormat.FMN_FORMAT.id == selectedFileFormat.id
        }

    val dsvFileFormatItems: Flow<List<DsvFileFormat>> =
        combine(
            currentFileFormat,
            fileImportStorage.flowOf(FileImportStorage::customFileFormats)
        ) { currentFileFormat: FileFormat, customFileFormats: CopyableCollection<FileFormat> ->
            FileFormat.predefinedFormats
                .filter { predefinedFileFormat: FileFormat ->
                    when (predefinedFileFormat.extension) {
                        FileFormat.EXTENSION_CSV, FileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .plus(customFileFormats)
                .map { predefinedFileFormat: FileFormat ->
                    DsvFileFormat(
                        predefinedFileFormat,
                        isSelected = predefinedFileFormat.id == currentFileFormat.id
                    )
                }
        }
}