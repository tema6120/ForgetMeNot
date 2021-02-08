package com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.sourcetext.ImportedTextEditorEvent.EncodingIsChanged

class ImportedTextEditorController(
    private val fileImporter: FileImporter,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<State>
) : BaseController<ImportedTextEditorEvent, Nothing>() {
    override fun handle(event: ImportedTextEditorEvent) {
        when (event) {
            is EncodingIsChanged -> {
                fileImporter.setCharset(event.newEncoding)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(fileImporter.state)
    }
}