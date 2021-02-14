package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatEvent.*

class FileFormatController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<FileFormatEvent, Nothing>() {
    override fun handle(event: FileFormatEvent) {
        when (event) {
            is FileFormatRadioButtonClicked -> {
                fileImporter.setFormat(event.fileFormat)
            }

            is ViewFileFormatSettingsButtonClicked -> {
                navigator.navigateToDsvFormat {
                    DsvFormatDiScope()
                }
            }

            is EditFileFormatSettingsButtonClicked -> {
                navigator.navigateToDsvFormat {
                    DsvFormatDiScope()
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}