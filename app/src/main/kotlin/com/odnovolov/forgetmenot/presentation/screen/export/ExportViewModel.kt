package com.odnovolov.forgetmenot.presentation.screen.export

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExportViewModel(
    dialogState: ExportDialogState,
    fileImportStorage: FileImportStorage
) {
    val stage: Flow<Stage> = dialogState.flowOf(ExportDialogState::stage)

    val dsvFileFormats: Flow<List<FileFormat>> = fileImportStorage
        .flowOf(FileImportStorage::customFileFormats)
        .map { customFileFormats: CopyableCollection<FileFormat> ->
            FileFormat.predefinedFormats
                .filter { predefinedFileFormat: FileFormat ->
                    when (predefinedFileFormat.extension) {
                        FileFormat.EXTENSION_CSV, FileFormat.EXTENSION_TSV -> true
                        else -> false
                    }
                }
                .plus(customFileFormats)
        }
}