package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImportStorage
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import kotlinx.coroutines.flow.*

class FileFormatViewModel(
    private val cardsFileId: Long,
    cardsImporterState: CardsImporter.State,
    cardsImportStorage: CardsImportStorage
) {
    private val cardsFile: Flow<CardsFile> =
        cardsImporterState.flowOf(CardsImporter.State::files)
            .map { files: List<CardsFile> ->
                files.find { file: CardsFile -> file.id == cardsFileId }
            }
            .filterNotNull()

    private val currentFileFormat: Flow<CardsFileFormat> =
        cardsFile.flatMapLatest { cardsFile: CardsFile ->
            cardsFile.flowOf(CardsFile::format)
        }

    val formatName: Flow<String> =
        currentFileFormat.map { format: CardsFileFormat ->
            if (format.isPredefined) {
                format.name
            } else {
                "'${format.name}'"
            }
        }

    val isFmnFormatSelected: Flow<Boolean> =
        currentFileFormat.map { selectedFileFormat: CardsFileFormat ->
            CardsFileFormat.FMN_FORMAT.id == selectedFileFormat.id
        }

    val dsvFileFormatItems: Flow<List<DsvFileFormat>> =
        combine(
            currentFileFormat,
            cardsImportStorage.flowOf(CardsImportStorage::customFileFormats)
        ) { currentFileFormat: CardsFileFormat, customFileFormats: CopyableCollection<CardsFileFormat> ->
            CardsFileFormat.predefinedFormats
                .filter { predefinedFileFormat: CardsFileFormat ->
                    when (predefinedFileFormat.extension) {
                        CardsFileFormat.EXTENSION_CSV, CardsFileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .plus(customFileFormats)
                .map { predefinedFileFormat: CardsFileFormat ->
                    DsvFileFormat(
                        predefinedFileFormat,
                        isSelected = predefinedFileFormat.id == currentFileFormat.id
                    )
                }
        }
}