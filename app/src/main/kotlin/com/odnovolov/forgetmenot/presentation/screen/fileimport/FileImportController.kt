package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*

class FileImportController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<FileImportEvent, Nothing>() {
    override fun handle(event: FileImportEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            DoneButtonClicked -> {
                val result = fileImporter.import()
                if (result[0]) {
                    navigator.navigateUp()
                }
            }

            is TextChanged -> {
                fileImporter.updateText(event.newText)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}