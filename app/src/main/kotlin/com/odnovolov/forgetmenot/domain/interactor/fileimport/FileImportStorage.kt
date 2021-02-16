package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class FileImportStorage(
    customFileFormats: MutableMap<Long, FileFormat>
) : FlowMakerWithRegistry<FileImportStorage>() {
    var customFileFormats: MutableMap<Long, FileFormat> by flowMaker(customFileFormats)

    override fun copy() = FileImportStorage(
        customFileFormats
    )
}