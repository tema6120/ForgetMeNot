package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange

class FileImportStorage(
    customFileFormats: CopyableCollection<FileFormat>
) : FlowMakerWithRegistry<FileImportStorage>() {
    var customFileFormats: CopyableCollection<FileFormat>
            by flowMaker(customFileFormats, CollectionChange::class)

    override fun copy() = FileImportStorage(
        customFileFormats
    )
}