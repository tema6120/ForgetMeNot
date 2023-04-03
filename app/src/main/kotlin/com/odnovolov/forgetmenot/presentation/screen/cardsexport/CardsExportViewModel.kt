package com.odnovolov.forgetmenot.presentation.screen.cardsexport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImportStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardsExportViewModel(
    dialogState: CardsExportDialogState,
    cardsImportStorage: CardsImportStorage
) {
    val stage: Flow<Stage> = dialogState.flowOf(CardsExportDialogState::stage)

    val dsvFileFormats: Flow<List<CardsFileFormat>> = cardsImportStorage
        .flowOf(CardsImportStorage::customFileFormats)
        .map { customFileFormats: CopyableCollection<CardsFileFormat> ->
            CardsFileFormat.predefinedFormats
                .filter { predefinedFileFormat: CardsFileFormat ->
                    when (predefinedFileFormat.extension) {
                        CardsFileFormat.EXTENSION_CSV, CardsFileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .plus(customFileFormats)
        }
}