package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import java.nio.charset.Charset

class FileImportSettings(
    charset: Charset
) : FlowMakerWithRegistry<FileImportSettings>() {
    var charset: Charset by flowMaker(charset)

    override fun copy() = FileImportSettings(charset)

    companion object {
        val DEFAULT_CHARSET = Charsets.UTF_8
    }
}